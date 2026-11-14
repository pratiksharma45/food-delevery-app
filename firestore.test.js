const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const serverTimestamp = () => new Date(Date.now() - 1000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const SELLER_A_UID = "seller_karim_uid";
const SELLER_B_UID = "seller_other_uid";
const CUSTOMER_A_UID = "customer_aarav_uid";
const CUSTOMER_B_UID = "customer_priya_uid";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validAddress() {
  return {
    fullName: "Aarav Sharma",
    phone: "9811234567",
    houseFlat: "B-42, 2nd Floor",
    streetColony: "Connaught Place",
    locality: "Connaught Place",
    landmark: "Near Metro Gate 4",
    pinCode: "110001",
    instructions: "Call on arrival",
  };
}

function validRestaurant(sellerUid) {
  return {
    sellerUid,
    name: "Karim's Royal Mughlai",
    locality: "Jama Masjid & CP",
    subRegion: "Central & Old Delhi",
    deliveryZoneIds: ["cp", "chandni_chowk"],
    cuisines: ["Biryani", "Mughlai"],
    rating: 4.6,
    reviewCountText: "12.4K+",
    deliveryTimeMins: 28,
    deliveryFeeInr: 25,
    priceForOneInr: 280,
    isPureVeg: false,
    offerText: "20% OFF up to ₹120",
    isRecommended: true,
    localImageKey: "img_food_biryani",
    isActive: true,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validDish(restaurantId, sellerUid) {
  return {
    restaurantId,
    sellerUid,
    name: "Old Delhi Mutton Dum Biryani",
    categoryId: "biryani",
    priceInr: 389,
    originalPriceInr: 449,
    isVeg: false,
    isBestseller: true,
    spiceLevel: "Medium Spicy",
    rating: 4.7,
    ratingCount: 1840,
    shortDescription: "Slow-cooked mutton & saffron basmati.",
    description: "Authentic Purani Dilli style slow-dum biryani.",
    whatYouWillGet: ["1 Clay Handi Biryani (600g)", "Mint Raita (120ml)"],
    serves: "Serves 1-2 people",
    prepTimeMins: 25,
    localImageKey: "img_food_biryani",
    isAvailable: true,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Public storefront read: unauthenticated customer can browse restaurants and dishes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("restaurants").get());
  await assertSucceeds(unauthDb.collection("dishes").get());
});

test("Unauthenticated isolation: cannot read customers, sellers, or orders, and cannot write anywhere", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("customers").doc(CUSTOMER_A_UID).get());
  await assertFails(unauthDb.collection("sellers").doc(SELLER_A_UID).get());
  await assertFails(unauthDb.collection("orders").where("customerUid", "==", CUSTOMER_A_UID).get());
  await assertFails(
    unauthDb.collection("restaurants").doc("rest_evil").set(validRestaurant(SELLER_A_UID))
  );
});

test("Seller ownership: Seller A creates restaurant & dish; Seller B cannot modify or add dish to Seller A's restaurant", async () => {
  const sellerADb = testEnv.authenticatedContext(SELLER_A_UID).firestore();
  const sellerBDb = testEnv.authenticatedContext(SELLER_B_UID).firestore();

  await assertSucceeds(
    sellerADb.collection("restaurants").doc("rest_karims").set(validRestaurant(SELLER_A_UID))
  );
  await assertSucceeds(
    sellerADb.collection("dishes").doc("dish_1").set(validDish("rest_karims", SELLER_A_UID))
  );

  // Seller B cannot edit Seller A's restaurant
  await assertFails(
    sellerBDb.collection("restaurants").doc("rest_karims").update({
      name: "Hijacked Restaurant",
      updatedAt: serverTimestamp(),
    })
  );

  // Seller B cannot add a dish under Seller A's restaurant
  await assertFails(
    sellerBDb.collection("dishes").doc("dish_evil").set(validDish("rest_karims", SELLER_B_UID))
  );

  // Seller A cannot inject ghost field
  await assertFails(
    sellerADb.collection("dishes").doc("dish_ghost").set({
      ...validDish("rest_karims", SELLER_A_UID),
      isVerifiedAdmin: true,
    })
  );
});

test("Customer PII isolation: customer can only read/write own profile", async () => {
  const custADb = testEnv.authenticatedContext(CUSTOMER_A_UID).firestore();
  const custBDb = testEnv.authenticatedContext(CUSTOMER_B_UID).firestore();

  await assertSucceeds(
    custADb.collection("customers").doc(CUSTOMER_A_UID).set({
      uid: CUSTOMER_A_UID,
      fullName: "Aarav Sharma",
      email: "aarav@delhi.in",
      phone: "9811234567",
      address: validAddress(),
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Customer B cannot read or list customers
  await assertFails(custBDb.collection("customers").doc(CUSTOMER_A_UID).get());
  await assertFails(custBDb.collection("customers").get());
});

test("Order sync & RBAC: customer creates COD order, seller updates status, other users blocked", async () => {
  const sellerADb = testEnv.authenticatedContext(SELLER_A_UID).firestore();
  const custADb = testEnv.authenticatedContext(CUSTOMER_A_UID).firestore();
  const custBDb = testEnv.authenticatedContext(CUSTOMER_B_UID).firestore();

  await assertSucceeds(
    sellerADb.collection("restaurants").doc("rest_karims").set(validRestaurant(SELLER_A_UID))
  );

  const validOrder = {
    customerUid: CUSTOMER_A_UID,
    restaurantId: "rest_karims",
    sellerUid: SELLER_A_UID,
    restaurantName: "Karim's Royal Mughlai",
    items: [{ dishId: "dish_1", dishName: "Mutton Biryani", quantity: 1, priceInr: 389, isVeg: false }],
    itemTotalInr: 389,
    deliveryFeeInr: 25,
    gstAndPackagingInr: 19,
    grandTotalInr: 433,
    deliveryAddress: validAddress(),
    paymentMethod: "Cash on Delivery (COD)",
    status: "PLACED",
    etaText: "25-35 mins",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };

  // Customer A places order
  await assertSucceeds(custADb.collection("orders").doc("ord_1001").set(validOrder));

  // Customer A cannot spoof sellerUid
  await assertFails(
    custADb.collection("orders").doc("ord_spoof").set({
      ...validOrder,
      sellerUid: CUSTOMER_A_UID,
    })
  );

  // Customer B cannot read Customer A's order or list all orders
  await assertFails(custBDb.collection("orders").doc("ord_1001").get());
  await assertFails(custBDb.collection("orders").get());

  // Customer A & Seller A can query their own orders
  await assertSucceeds(custADb.collection("orders").where("customerUid", "==", CUSTOMER_A_UID).get());
  await assertSucceeds(sellerADb.collection("orders").where("sellerUid", "==", SELLER_A_UID).get());

  // Seller A can advance status to PREPARING
  await assertSucceeds(
    sellerADb.collection("orders").doc("ord_1001").update({
      status: "PREPARING",
      etaText: "15-20 mins",
      updatedAt: serverTimestamp(),
    })
  );

  // Seller A cannot tamper with grandTotalInr
  await assertFails(
    sellerADb.collection("orders").doc("ord_1001").update({
      grandTotalInr: 9999,
      updatedAt: serverTimestamp(),
    })
  );
});
