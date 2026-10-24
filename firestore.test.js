const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

test("Unauthenticated user: cannot read users or posts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
  await assertFails(unauthDb.collection("posts").get());
});

test("Authenticated user: can create own profile with valid data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice_vyro",
      displayName: "Alice V",
      bio: "Creating on VYRO",
      avatarUrl: "https://example.com/avatar.jpg",
      followersCount: 0,
      followingCount: 0,
      postsCount: 0,
      isPrivate: false,
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: cannot impersonate or write someone else profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      username: "bob_vyro",
      displayName: "Bob V",
      followersCount: 0,
      followingCount: 0,
      postsCount: 0,
      isPrivate: false,
      createdAt: new Date(),
    })
  );
});

test("Conversation member: can access conversation and messages", async () => {
  // Create conversation as Alice
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const convRef = aliceDb.collection("conversations").doc("conv_1");
  await assertSucceeds(
    convRef.set({
      type: "direct",
      creatorId: ALICE_UID,
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Bob can read it
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("conversations").doc("conv_1").get());

  // Charlie (not a member) cannot read it
  const charlieDb = testEnv.authenticatedContext("charlie_789").firestore();
  await assertFails(charlieDb.collection("conversations").doc("conv_1").get());
});
