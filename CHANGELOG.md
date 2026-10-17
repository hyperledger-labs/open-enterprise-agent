## [2.2.0](https://github.com/hyperledger-identus/cloud-agent/compare/v2.1.0...v2.2.0) (2026-06-09)

### Features

* add PRISM VDR driver integration ([#1671](https://github.com/hyperledger-identus/cloud-agent/issues/1671)) ([f48add8](https://github.com/hyperledger-identus/cloud-agent/commit/f48add8b8acd28460ab89c6f9a6cb03f441484e1))
* add VDR capabilities to the cloud-agent using the prism-node ([#1685](https://github.com/hyperledger-identus/cloud-agent/issues/1685)) ([9931f22](https://github.com/hyperledger-identus/cloud-agent/commit/9931f22ecfc5faa1c9025ac14317572d233f0cdd))
* support NeoPRISM as node backend ([#1693](https://github.com/hyperledger-identus/cloud-agent/issues/1693)) ([e1a5af2](https://github.com/hyperledger-identus/cloud-agent/commit/e1a5af208644e95fc3b4e1e9c8f0d35e3d5876d4))
* token permissions and remove codeql ([#1665](https://github.com/hyperledger-identus/cloud-agent/issues/1665)) ([2f2f1ca](https://github.com/hyperledger-identus/cloud-agent/commit/2f2f1ca69a175a4003d6014788e48c8ed6a5ab10))
* VDR driver leftovers and improvements ([#1712](https://github.com/hyperledger-identus/cloud-agent/issues/1712)) ([679e1b8](https://github.com/hyperledger-identus/cloud-agent/commit/679e1b84fa9982596c15a813914791d6f3ecd78b))
* **vdr:** add NeoPrism REST API as VDR backend ([#1728](https://github.com/hyperledger-identus/cloud-agent/issues/1728)) ([e7bddd3](https://github.com/hyperledger-identus/cloud-agent/commit/e7bddd3ee4aa4c1dd9f29c37367b2777c58fcf17))

### Bug Fixes

* add allstar exception for thr binaries ([#1683](https://github.com/hyperledger-identus/cloud-agent/issues/1683)) ([c5980cb](https://github.com/hyperledger-identus/cloud-agent/commit/c5980cb67023a5ec99d02e21757b8d8c7966b9eb))
* **ci:** update neoprism version and fix VDR cucumber tag ([#1729](https://github.com/hyperledger-identus/cloud-agent/issues/1729)) ([1224017](https://github.com/hyperledger-identus/cloud-agent/commit/122401756b3f3dbd449086a417738bd8a7c0db19))
* correct typos, API endpoint documentation and error responses ([#1768](https://github.com/hyperledger-identus/cloud-agent/issues/1768)) ([18761bd](https://github.com/hyperledger-identus/cloud-agent/commit/18761bd7bcd5f93a6887dae1fef6ec7ca9c8c5c4))
* docker image ([#1639](https://github.com/hyperledger-identus/cloud-agent/issues/1639)) ([7a19081](https://github.com/hyperledger-identus/cloud-agent/commit/7a190813b3d805ed8794278afc254d16fa7bb9e1))
* **docs:** correct glossary link paths in connectionless present-proof ([#1796](https://github.com/hyperledger-identus/cloud-agent/issues/1796)) ([e1e8be1](https://github.com/hyperledger-identus/cloud-agent/commit/e1e8be1e244eb9dbf8cc00650d29e63e3274491f))
* **docs:** correct relative link path in VDR manage tutorial ([#1737](https://github.com/hyperledger-identus/cloud-agent/issues/1737)) ([55a5e4e](https://github.com/hyperledger-identus/cloud-agent/commit/55a5e4ef71bdad37b1181400b392bc5b2106a327))
* pipeline script ([#1676](https://github.com/hyperledger-identus/cloud-agent/issues/1676)) ([86ac86f](https://github.com/hyperledger-identus/cloud-agent/commit/86ac86fa0316c268ad73e4db3ec4d0c6acc51127))
* remove checksum bypass and pin java-multibase to v1.1.1 ([#1769](https://github.com/hyperledger-identus/cloud-agent/issues/1769)) ([dddf7fb](https://github.com/hyperledger-identus/cloud-agent/commit/dddf7fb613271e77d88a002665099ef0214477e3))
* **tests:** pin published snapshot and drop invalid github packages repo ([#1805](https://github.com/hyperledger-identus/cloud-agent/issues/1805)) ([81d2cb3](https://github.com/hyperledger-identus/cloud-agent/commit/81d2cb38f3ab8a27c3ba0dd74769b39903050e03))
* token permissions ([#1666](https://github.com/hyperledger-identus/cloud-agent/issues/1666)) ([518c094](https://github.com/hyperledger-identus/cloud-agent/commit/518c094c5c74a53e9ee2b2a7a9db1a26de06ff8a))
* Token-Permissions  ([#1678](https://github.com/hyperledger-identus/cloud-agent/issues/1678)) ([adc81b1](https://github.com/hyperledger-identus/cloud-agent/commit/adc81b1d2a1a10b3c6e6b778944f2c5fca4c0673))

## [2.1.0](https://github.com/hyperledger-identus/cloud-agent/compare/v2.0.0...v2.1.0) (2025-10-28)

### Features

* add timer and metric(histogram) to measure job execution duration ([#1576](https://github.com/hyperledger-identus/cloud-agent/issues/1576)) ([1428c67](https://github.com/hyperledger-identus/cloud-agent/commit/1428c6765b5ee2223e3c271a1b9ff5700331eb97))
* vdr integration ([#1591](https://github.com/hyperledger-identus/cloud-agent/issues/1591)) ([16b89d8](https://github.com/hyperledger-identus/cloud-agent/commit/16b89d83ea1481047e781c3e17ea610da6f3fcc4))

### Bug Fixes

* update broken links in oas ([#1578](https://github.com/hyperledger-identus/cloud-agent/issues/1578)) ([ebaf912](https://github.com/hyperledger-identus/cloud-agent/commit/ebaf91293182ee56767776c355e8a018a86efb11))
* update default value for API_KEY_AUTO_PROVISIONING to false ([#1628](https://github.com/hyperledger-identus/cloud-agent/issues/1628)) ([67ee436](https://github.com/hyperledger-identus/cloud-agent/commit/67ee436153275d5fc3a2c2b233bdcba417e41770))
* update examples to ensure they work correctly ([#1625](https://github.com/hyperledger-identus/cloud-agent/issues/1625)) ([b8f58c6](https://github.com/hyperledger-identus/cloud-agent/commit/b8f58c6337d56bd906c8abe8e5e2fe79ad0be45f))
* update key selection logic to accept Ed25519 in status-list and proof jobs ([#1616](https://github.com/hyperledger-identus/cloud-agent/issues/1616)) ([0b7ac9c](https://github.com/hyperledger-identus/cloud-agent/commit/0b7ac9c26f0e6cc1f94c3264ea7be318b82e4d4e))
* use custom vdr scheme for vdr endpoints ([#1600](https://github.com/hyperledger-identus/cloud-agent/issues/1600)) ([fd409b5](https://github.com/hyperledger-identus/cloud-agent/commit/fd409b51a44113552e22bd097561487361b4df94))

## [2.0.0](https://github.com/hyperledger-identus/cloud-agent/compare/v1.40.0...v2.0.0) (2025-05-05)

### ⚠ BREAKING CHANGES

* trigger major version release (#1570)

### Features

* add e2e tests for different createCredentialOffer API variations ([#1476](https://github.com/hyperledger-identus/cloud-agent/issues/1476)) ([2ce192e](https://github.com/hyperledger-identus/cloud-agent/commit/2ce192e1fc874f5756e35e86f868c25f524792be))
* add feature flag to enable/disable support for Anoncred (backend job and API) ([#1492](https://github.com/hyperledger-identus/cloud-agent/issues/1492)) ([54b7b5b](https://github.com/hyperledger-identus/cloud-agent/commit/54b7b5b77950f00d30f9bdade88c2ce8e9525bde)), closes [#1491](https://github.com/hyperledger-identus/cloud-agent/issues/1491)
* add LOG_LEVEL environment variable to the cloud-agent logback.x… ([#1504](https://github.com/hyperledger-identus/cloud-agent/issues/1504)) ([fa1c04f](https://github.com/hyperledger-identus/cloud-agent/commit/fa1c04f501948401d62c4e37dd14b320fc224dd9))
* align the credential schema property name according to the VCDM 1.1 ([#1467](https://github.com/hyperledger-identus/cloud-agent/issues/1467)) ([c6a3e0c](https://github.com/hyperledger-identus/cloud-agent/commit/c6a3e0ced12927f5a819af9f4f56e8e381293d15))
* delete endpoint for user to delete cloud-agent connection ([#1568](https://github.com/hyperledger-identus/cloud-agent/issues/1568)) ([8796be1](https://github.com/hyperledger-identus/cloud-agent/commit/8796be1ac6ee301ea55030cf3d4814918aa728d8))
* disable the APIKEY authentication in the cloud-agent ([#1479](https://github.com/hyperledger-identus/cloud-agent/issues/1479)) ([2fb833a](https://github.com/hyperledger-identus/cloud-agent/commit/2fb833abbafcad5ca7511fd59e52d6d009e2d5ab))
* migrate mercury protocols and repositories from Circe to ZIO Json ([#1471](https://github.com/hyperledger-identus/cloud-agent/issues/1471)) ([d0c3506](https://github.com/hyperledger-identus/cloud-agent/commit/d0c3506ed04b8aeab5c007c33ede61ca48800234))
* migrate pollux VC JWT from Circe to zio-json ([#1475](https://github.com/hyperledger-identus/cloud-agent/issues/1475)) ([248ba5f](https://github.com/hyperledger-identus/cloud-agent/commit/248ba5fe14fe2736b00fa42f72aeffe523362406))
* share Kafka producers among threads ([#1460](https://github.com/hyperledger-identus/cloud-agent/issues/1460)) ([5ece0dd](https://github.com/hyperledger-identus/cloud-agent/commit/5ece0dd573611c2f8f72f12c76a00aebc7841faf))
* support 'pollux.domain' config param ([#1483](https://github.com/hyperledger-identus/cloud-agent/issues/1483)) ([81350ea](https://github.com/hyperledger-identus/cloud-agent/commit/81350ea247f9d2b29a9ad246e882a60f9dc51fd8))

### Bug Fixes

* Add disclose claims to presentation record credential type SDJWT  ([#1487](https://github.com/hyperledger-identus/cloud-agent/issues/1487)) ([5e00556](https://github.com/hyperledger-identus/cloud-agent/commit/5e005560c62d4f50fdad4b0b7cede355cced9753))
* add LRUDocumentLoader to optimize the JsonLD.toRdf call, fix [#1553](https://github.com/hyperledger-identus/cloud-agent/issues/1553) ([#1560](https://github.com/hyperledger-identus/cloud-agent/issues/1560)) ([c710196](https://github.com/hyperledger-identus/cloud-agent/commit/c7101966c51dfbda4c4d831c24739c566637aa52))
* add SDJWT value to the OAS enum validation in IssueCredentialRec… ([#1533](https://github.com/hyperledger-identus/cloud-agent/issues/1533)) ([0b6fbc8](https://github.com/hyperledger-identus/cloud-agent/commit/0b6fbc8d83c2f57a899d69c2f2574ee27d8644a9))
* broken links in the docusaurus documentation files ([#1489](https://github.com/hyperledger-identus/cloud-agent/issues/1489)) ([38b251b](https://github.com/hyperledger-identus/cloud-agent/commit/38b251b2083e6224d8c81d3f183af9e55af57d94))
* code cleanup ([#1539](https://github.com/hyperledger-identus/cloud-agent/issues/1539)) ([75bc1ce](https://github.com/hyperledger-identus/cloud-agent/commit/75bc1cef8aca36be2188a49be75f2bbcbccdf28e))
* Commit message guideline broken link fixed ([#1542](https://github.com/hyperledger-identus/cloud-agent/issues/1542)) ([fa21e86](https://github.com/hyperledger-identus/cloud-agent/commit/fa21e8688debaaf6d403031473895048343ce8dd))
* createCredentialIssueRecordRequest schemaId field to be Option[String] ([#1481](https://github.com/hyperledger-identus/cloud-agent/issues/1481)) ([7524ec7](https://github.com/hyperledger-identus/cloud-agent/commit/7524ec7e12bcbcd1cf9bf0c44fe6c8d11abd8204))
* docker compose to pass  enable API_KEY_ENABLED flag ([#1531](https://github.com/hyperledger-identus/cloud-agent/issues/1531)) ([a1a545a](https://github.com/hyperledger-identus/cloud-agent/commit/a1a545a37ab3ceb4ea59d812cb9e0563820466bd))
* documentation build ([#1488](https://github.com/hyperledger-identus/cloud-agent/issues/1488)) ([b947a8b](https://github.com/hyperledger-identus/cloud-agent/commit/b947a8b020dd83aa6f4e88278ed2afe9c4c687f2))
* Kafka consumer not picking messages ([#1441](https://github.com/hyperledger-identus/cloud-agent/issues/1441)) ([b0effec](https://github.com/hyperledger-identus/cloud-agent/commit/b0effec9bdc48f7daad94a159c78766c6ed1c903))
* LOG_LEVEL environment variable and cleanup logging entries ([#1562](https://github.com/hyperledger-identus/cloud-agent/issues/1562)) ([7c57df0](https://github.com/hyperledger-identus/cloud-agent/commit/7c57df0fd0ccf0460169ddd7f894c2e86580db6c))
* mapping the claims attributes to json ([#1480](https://github.com/hyperledger-identus/cloud-agent/issues/1480)) ([a96e544](https://github.com/hyperledger-identus/cloud-agent/commit/a96e544ea8acee8a2fa2f6a5a6e818c4bc1d8bb0))
* **migration:** quote reserved "format" column for PostgreSQL compatibility ([#1554](https://github.com/hyperledger-identus/cloud-agent/issues/1554)) ([06c7c52](https://github.com/hyperledger-identus/cloud-agent/commit/06c7c5244eb3768db648369cd75956c90d9b5b31))
* mitigation to [#1495](https://github.com/hyperledger-identus/cloud-agent/issues/1495) (update the outdated url) ([#1549](https://github.com/hyperledger-identus/cloud-agent/issues/1549)) ([27c7f97](https://github.com/hyperledger-identus/cloud-agent/commit/27c7f9773f4ee5732b69f54869af988a88e7e211))
* openAPI mutability [#1115](https://github.com/hyperledger-identus/cloud-agent/issues/1115) ([#1494](https://github.com/hyperledger-identus/cloud-agent/issues/1494)) ([1e19b04](https://github.com/hyperledger-identus/cloud-agent/commit/1e19b048a49132cb2402c27304660c7806f85170))
* proof presentation request ([#1453](https://github.com/hyperledger-identus/cloud-agent/issues/1453)) ([7839b95](https://github.com/hyperledger-identus/cloud-agent/commit/7839b95ad423556129a4d7c1bf90b75dfa799db6))
* remove adding default service ([#1538](https://github.com/hyperledger-identus/cloud-agent/issues/1538)) ([254ee94](https://github.com/hyperledger-identus/cloud-agent/commit/254ee944f169e8fea0d87289238f7f6c0bf5f625))
* return proper error response for unauthorized access ([#1540](https://github.com/hyperledger-identus/cloud-agent/issues/1540)) ([a507da7](https://github.com/hyperledger-identus/cloud-agent/commit/a507da77a9198ccdfd01dab5a57efdf2f81a4b30))
* update the jwt to have shortform of prism did ([#1461](https://github.com/hyperledger-identus/cloud-agent/issues/1461)) ([7365497](https://github.com/hyperledger-identus/cloud-agent/commit/73654975bf9f2ad19f47d7220d61a38fed55dd42))
* VPJWT JSON decoder in the issue-credential protocol ([#1505](https://github.com/hyperledger-identus/cloud-agent/issues/1505)) ([d16da04](https://github.com/hyperledger-identus/cloud-agent/commit/d16da0461b6aaac209d5334c25eaf22b8641beed))
* Warning on Invitation generated  on Issuance ([#1433](https://github.com/hyperledger-identus/cloud-agent/issues/1433)) ([05fdf26](https://github.com/hyperledger-identus/cloud-agent/commit/05fdf2624463227ad7b9452e52bc9d9308d3a3ad))

### Miscellaneous Chores

* trigger major version release ([#1570](https://github.com/hyperledger-identus/cloud-agent/issues/1570)) ([aa2359c](https://github.com/hyperledger-identus/cloud-agent/commit/aa2359c43acb0807bb9f11ed0475bacae8566efe))

## [1.40.0](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.39.0...cloud-agent-v1.40.0) (2024-11-05)

### Features

* Add KID to the credential-offers API - ATL-7704 ([#1320](https://github.com/hyperledger-identus/cloud-agent/issues/1320)) ([56200cf](https://github.com/hyperledger-identus/cloud-agent/commit/56200cfae6f62b823a74e67eb060face2ee3ecbc))
* add presentation-exchange endpoints ([#1365](https://github.com/hyperledger-identus/cloud-agent/issues/1365)) ([49f7ab3](https://github.com/hyperledger-identus/cloud-agent/commit/49f7ab3d0473d820c37dc7f4f944cf1c2cae2a25))
* ATL-6983 ZIO Stream Kafka PoC in background jobs ([#1339](https://github.com/hyperledger-identus/cloud-agent/issues/1339)) ([19ab426](https://github.com/hyperledger-identus/cloud-agent/commit/19ab426a191eec575ffebe6a2417f3fce538969c))
* Default Backend API to Array Of Credential Schema ([#1366](https://github.com/hyperledger-identus/cloud-agent/issues/1366)) ([693dcc4](https://github.com/hyperledger-identus/cloud-agent/commit/693dcc45274044ac9bebffe2a8dbe0b85b45b452))
* Default Object As Issuer ([#1349](https://github.com/hyperledger-identus/cloud-agent/issues/1349)) ([d29eebb](https://github.com/hyperledger-identus/cloud-agent/commit/d29eebbef29773103814528c382a3000c4f3b29b))
* Implement prism anoncreds method for schemas and credential definitions ([#1385](https://github.com/hyperledger-identus/cloud-agent/issues/1385)) ([fbee055](https://github.com/hyperledger-identus/cloud-agent/commit/fbee0554bf424acf8007c9b7088cdb0654f0d6b2))
* Issuer Replace Either By Union Type ([#1374](https://github.com/hyperledger-identus/cloud-agent/issues/1374)) ([8fc2fe3](https://github.com/hyperledger-identus/cloud-agent/commit/8fc2fe3dbed8856d21c18b7fedf89454661b34d6))
* presentation_submission validation logic ([#1332](https://github.com/hyperledger-identus/cloud-agent/issues/1332)) ([f80b3c3](https://github.com/hyperledger-identus/cloud-agent/commit/f80b3c34588437b131ce872fd86f93e75dcd035f))
* Support Array Of Credential Schema ([#1351](https://github.com/hyperledger-identus/cloud-agent/issues/1351)) ([948e314](https://github.com/hyperledger-identus/cloud-agent/commit/948e3149466b327686273825ce7858adaf8d7555))
* Test JWT OBJECT as Issuer ([#1343](https://github.com/hyperledger-identus/cloud-agent/issues/1343)) ([7208d95](https://github.com/hyperledger-identus/cloud-agent/commit/7208d955b56375b0c79c20a0237df9890ecd3580))
* VC support for Array of credential Status ([#1383](https://github.com/hyperledger-identus/cloud-agent/issues/1383)) ([ad946cf](https://github.com/hyperledger-identus/cloud-agent/commit/ad946cf3f635b882d772a00b0202b957a1cb82cb))
* VCVerification API support ARRAY or OBJECT as Credential Sc… ([#1355](https://github.com/hyperledger-identus/cloud-agent/issues/1355)) ([91cb4e7](https://github.com/hyperledger-identus/cloud-agent/commit/91cb4e7f4371a651617265279a27fefe9551887c))

### Bug Fixes

* Add key_id missing field ([#1403](https://github.com/hyperledger-identus/cloud-agent/issues/1403)) ([cbd1a03](https://github.com/hyperledger-identus/cloud-agent/commit/cbd1a03a8aa91c5a5487c54046e4d9305f9d9241))
* adjust Kotlin and TypeScript HTTP client to use the `schemaId` f… ([#1388](https://github.com/hyperledger-identus/cloud-agent/issues/1388)) ([c2da492](https://github.com/hyperledger-identus/cloud-agent/commit/c2da492131e5c545b0fefb101246c48684bc9433))
* cannot reuse the same credential-offer in oid4vci ([#1361](https://github.com/hyperledger-identus/cloud-agent/issues/1361)) ([6a0a3ea](https://github.com/hyperledger-identus/cloud-agent/commit/6a0a3ea3deef712479420ac23ef58aaafa7df78a))
* handle unsupported PIURI found in DIDComm messages accordingly ([#1399](https://github.com/hyperledger-identus/cloud-agent/issues/1399)) ([9b64793](https://github.com/hyperledger-identus/cloud-agent/commit/9b64793ee7939860973108a8b30bc0b48a840518))
* key id for jwt and sdjwt ([#1420](https://github.com/hyperledger-identus/cloud-agent/issues/1420)) ([5830a7e](https://github.com/hyperledger-identus/cloud-agent/commit/5830a7e17a72abae98faa81594421aa577eaeb24))
* oas to use any schema for json ast node ([#1372](https://github.com/hyperledger-identus/cloud-agent/issues/1372)) ([95d328e](https://github.com/hyperledger-identus/cloud-agent/commit/95d328e3420d4731817a1f91c720e2833e9de362))
* oid4vci endpoints error statuses and negative input validation ([#1384](https://github.com/hyperledger-identus/cloud-agent/issues/1384)) ([65cc9a7](https://github.com/hyperledger-identus/cloud-agent/commit/65cc9a712af722f5cb3dd36e78b088c20723097b))
* Preserve Presentation Format ([#1363](https://github.com/hyperledger-identus/cloud-agent/issues/1363)) ([c18385c](https://github.com/hyperledger-identus/cloud-agent/commit/c18385c8fdbbb0e5dbde9a03e21f4600bf5e6890))
* return 404 when create credConfig on non-existing issuer ([#1379](https://github.com/hyperledger-identus/cloud-agent/issues/1379)) ([e532ba6](https://github.com/hyperledger-identus/cloud-agent/commit/e532ba604c4e8e820345226d842d3b27813f5e66))

## [1.39.0](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.38.0...cloud-agent-v1.39.0) (2024-09-11)

### Features

* API Supports ValidFrom And ValidTo ([#1298](https://github.com/hyperledger-identus/cloud-agent/issues/1298)) ([b19f514](https://github.com/hyperledger-identus/cloud-agent/commit/b19f514d3e6bf762fa566dd4f0024e8732778911))
* API Supports ValidFrom And ValidTo (PART 2) ([#1302](https://github.com/hyperledger-identus/cloud-agent/issues/1302)) ([b0c58f3](https://github.com/hyperledger-identus/cloud-agent/commit/b0c58f3c3a2fd6fa9c5364f894737bf301f4dea9))
* API Supports ValidFrom And ValidTo Test ([#1305](https://github.com/hyperledger-identus/cloud-agent/issues/1305)) ([3a7984b](https://github.com/hyperledger-identus/cloud-agent/commit/3a7984b180989cad00e8511f9b564a51dae268f2))
* connection less issuance ([#1301](https://github.com/hyperledger-identus/cloud-agent/issues/1301)) ([594f7c9](https://github.com/hyperledger-identus/cloud-agent/commit/594f7c910f4cbe8990263b4154490d3ad0ed169d))
* connection less presentation ([#1285](https://github.com/hyperledger-identus/cloud-agent/issues/1285)) ([c5846d1](https://github.com/hyperledger-identus/cloud-agent/commit/c5846d15cbe1cfd4b9776ad7c259962182057e0c))
* connection less presentation expiration time ([#1294](https://github.com/hyperledger-identus/cloud-agent/issues/1294)) ([6024c97](https://github.com/hyperledger-identus/cloud-agent/commit/6024c97269ad23b99c5e238c97179da095cb91dc))
* Expose Stored Error ([#1276](https://github.com/hyperledger-identus/cloud-agent/issues/1276)) ([758fe87](https://github.com/hyperledger-identus/cloud-agent/commit/758fe87cb3c729b544a1df434c23d535162cbba9))
* Fix Object causing StatusList to fail ([#1322](https://github.com/hyperledger-identus/cloud-agent/issues/1322)) ([fb32d6e](https://github.com/hyperledger-identus/cloud-agent/commit/fb32d6eb732c9c3ac2ad7d9cf3f1a388400a4b6b))
* Handle Error Notify webhooks ATL-6934 ([#1279](https://github.com/hyperledger-identus/cloud-agent/issues/1279)) ([7c31a9d](https://github.com/hyperledger-identus/cloud-agent/commit/7c31a9d4ec3e63273f804c715ae96791b5aa50c9))
* integrate json-path in presentation definition ([#1311](https://github.com/hyperledger-identus/cloud-agent/issues/1311)) ([9ef6b09](https://github.com/hyperledger-identus/cloud-agent/commit/9ef6b09e8f09cd52cdb67003506b3ef15c58d919))
* Move ADRs to the identus-docs repo ([#1284](https://github.com/hyperledger-identus/cloud-agent/issues/1284)) ([4d5ca64](https://github.com/hyperledger-identus/cloud-agent/commit/4d5ca6419ac416c5851cc2a09b1cca1af572d55b))
* postgres metrics ([#1274](https://github.com/hyperledger-identus/cloud-agent/issues/1274)) ([cf3ccbe](https://github.com/hyperledger-identus/cloud-agent/commit/cf3ccbefdd8ab3f1f18e80303c910f72661cfc42))
* presentation-exchange model and json schema refactoring ([#1304](https://github.com/hyperledger-identus/cloud-agent/issues/1304)) ([75b2736](https://github.com/hyperledger-identus/cloud-agent/commit/75b2736b71739be7c57c582f64b6845da9a6bd8c))
* URL or Object as Issuer ([#1321](https://github.com/hyperledger-identus/cloud-agent/issues/1321)) ([0c53bba](https://github.com/hyperledger-identus/cloud-agent/commit/0c53bbaf3b7753015b5876001002b7a9ba54ccfb))

### Bug Fixes

* [#1259](https://github.com/hyperledger-identus/cloud-agent/issues/1259) make GITHUB_TOKEN optional ([#1275](https://github.com/hyperledger-identus/cloud-agent/issues/1275)) ([1c9cbd0](https://github.com/hyperledger-identus/cloud-agent/commit/1c9cbd0a124cdf5626605e1e4419130f885364a7))
* add License to the POM files [#1099](https://github.com/hyperledger-identus/cloud-agent/issues/1099) ([#1310](https://github.com/hyperledger-identus/cloud-agent/issues/1310)) ([5a7b950](https://github.com/hyperledger-identus/cloud-agent/commit/5a7b9508c9d62e57ac6a98da6726b382587478bf))
* add reportProcessingFailure back in PresentationRepository ([#1232](https://github.com/hyperledger-identus/cloud-agent/issues/1232)) ([d22745f](https://github.com/hyperledger-identus/cloud-agent/commit/d22745fc9589c71d038af60d8fc2c99d8cbd104a))
* bitString base64 encoding for revocation status list ([#1273](https://github.com/hyperledger-identus/cloud-agent/issues/1273)) ([45e0613](https://github.com/hyperledger-identus/cloud-agent/commit/45e0613ea42fb21786562c7f44b40f63cbdae6dc))
* changed IO to UIO as as underline repository doesn't throw error ([#1271](https://github.com/hyperledger-identus/cloud-agent/issues/1271)) ([2aba639](https://github.com/hyperledger-identus/cloud-agent/commit/2aba639aa77cfe293b11fabca5c458a220576435))
* cleanup and minor refactoring to remove duplicates ([#1309](https://github.com/hyperledger-identus/cloud-agent/issues/1309)) ([238492b](https://github.com/hyperledger-identus/cloud-agent/commit/238492b8af2e131ae9dde058ea6a57df5787c1b1))
* delete subject id from presentation record ([#1314](https://github.com/hyperledger-identus/cloud-agent/issues/1314)) ([b73b806](https://github.com/hyperledger-identus/cloud-agent/commit/b73b8066651f0685fd8b2fd46b1e12d4adfe4156))
* GET Requested present proof by Holder ([#1316](https://github.com/hyperledger-identus/cloud-agent/issues/1316)) ([3b3da2c](https://github.com/hyperledger-identus/cloud-agent/commit/3b3da2c3a14a7d48fca38f286153ceeca4839384))
* improve k8s keycloak bootstrapping script ([#1278](https://github.com/hyperledger-identus/cloud-agent/issues/1278)) ([cfc4ccf](https://github.com/hyperledger-identus/cloud-agent/commit/cfc4ccf3b10f2a59471f107e0b3cfc4ac568f5c4))
* migrate to quill for generic secret storage ([#1299](https://github.com/hyperledger-identus/cloud-agent/issues/1299)) ([e077cdd](https://github.com/hyperledger-identus/cloud-agent/commit/e077cdd016954d028b702f2a107e9165005beb5c))
* migrate wallet nonsecret storage to quill ([#1290](https://github.com/hyperledger-identus/cloud-agent/issues/1290)) ([525b3bc](https://github.com/hyperledger-identus/cloud-agent/commit/525b3bcb7006599d873e8a089e8f03da361e74eb))
* misc spelling ([#1288](https://github.com/hyperledger-identus/cloud-agent/issues/1288)) ([88efa9c](https://github.com/hyperledger-identus/cloud-agent/commit/88efa9ca1bc323af4cac35fb3096ac44b74e74bc))
* operation id repeated error ([#1306](https://github.com/hyperledger-identus/cloud-agent/issues/1306)) ([8e39d0b](https://github.com/hyperledger-identus/cloud-agent/commit/8e39d0bbdbd7d4d087865651c4c8ce3b16540174))
* remove `prism-agent` path from the apisixroute.yaml ([#1330](https://github.com/hyperledger-identus/cloud-agent/issues/1330)) ([82b9d1d](https://github.com/hyperledger-identus/cloud-agent/commit/82b9d1d38f0de0a381b9a6c6569411786630ca4b))
* remove deprecation warnings and optimize tests performance ([#1315](https://github.com/hyperledger-identus/cloud-agent/issues/1315)) ([7558245](https://github.com/hyperledger-identus/cloud-agent/commit/75582453454c80e35080817dbe8b1bb02c4a94b6))
* Remove type DID ([#1327](https://github.com/hyperledger-identus/cloud-agent/issues/1327)) ([1ed2a14](https://github.com/hyperledger-identus/cloud-agent/commit/1ed2a14651b6411add88f96208db6037b49ebd4a))
* rename folder from atala to identus ([#1270](https://github.com/hyperledger-identus/cloud-agent/issues/1270)) ([12660ef](https://github.com/hyperledger-identus/cloud-agent/commit/12660ef81a8f057bd63f1a6fa8cc775cc2459cb1))
* replace problematic dependency license from Apollo ([#1312](https://github.com/hyperledger-identus/cloud-agent/issues/1312)) ([11ee9df](https://github.com/hyperledger-identus/cloud-agent/commit/11ee9df88c789c36c6488ad1409c46cd43fcc7d5))
* the oob encode invitation ([#1313](https://github.com/hyperledger-identus/cloud-agent/issues/1313)) ([f2313f2](https://github.com/hyperledger-identus/cloud-agent/commit/f2313f23189879d8f2f3a548ee1b27c6ecb0c0be))

## [1.38.0](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.37.0...cloud-agent-v1.38.0) (2024-07-15)

### Features

* upgrade docusaurus and semantic-release packages ([de53f1d](https://github.com/hyperledger-identus/cloud-agent/commit/de53f1db15a25e4d66cba1b191fc6e591b42284b))

### Bug Fixes

* Move InMemory classes to the test moduels ([#1240](https://github.com/hyperledger-identus/cloud-agent/issues/1240)) ([823057a](https://github.com/hyperledger-identus/cloud-agent/commit/823057adaa6127eca80dba4df123f07098d34f65))
* move mocks into the test modules ([#1236](https://github.com/hyperledger-identus/cloud-agent/issues/1236)) ([df83026](https://github.com/hyperledger-identus/cloud-agent/commit/df83026704980e071f7aa158634da20fbc2527c3))
* use Put and Get for DID in doobie statement ([#1250](https://github.com/hyperledger-identus/cloud-agent/issues/1250)) ([fc1cf51](https://github.com/hyperledger-identus/cloud-agent/commit/fc1cf5157f5503143c23da54c8ea6fe78a776640))
* Wallet Management Error Handling ([#1248](https://github.com/hyperledger-identus/cloud-agent/issues/1248)) ([cfd5101](https://github.com/hyperledger-identus/cloud-agent/commit/cfd5101f18276b9f59830c47c0d7fa64b30662db))

## [1.37.0](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.36.1...cloud-agent-v1.37.0) (2024-07-01)

### Features

* add oidc4vci protocol MVP ([#1182](https://github.com/hyperledger-identus/cloud-agent/issues/1182)) ([3ae91dc](https://github.com/hyperledger-identus/cloud-agent/commit/3ae91dce17e5925aad8d5ce3471527889f26c6de))
* add schemaId to the issuance session ([#1199](https://github.com/hyperledger-identus/cloud-agent/issues/1199)) ([97f5d83](https://github.com/hyperledger-identus/cloud-agent/commit/97f5d833d7988d59c4fd51459ee3d48d779399a3))
* add support for EcdsaSecp256k1Signature2019Proof and fix pk encoding for EddsaJcs2022Proof ([#1127](https://github.com/hyperledger-identus/cloud-agent/issues/1127)) ([e617ded](https://github.com/hyperledger-identus/cloud-agent/commit/e617dedd962f379033ae199d40addd222bf945da))
* **agent:** add browser fingerprint label to http metrics ([#1231](https://github.com/hyperledger-identus/cloud-agent/issues/1231)) ([f090554](https://github.com/hyperledger-identus/cloud-agent/commit/f09055455df7ebc316fbc6f0e6bf610a05a278a1))
* ATL 6829 - Integrate ZIO failures and defects ADR in credential status list ([#1175](https://github.com/hyperledger-identus/cloud-agent/issues/1175)) ([dffad1d](https://github.com/hyperledger-identus/cloud-agent/commit/dffad1d0bc07248f1559651ddce82629231c6cf7))
* ATL-6832 ZIO failures and defects in entity controller ([#1203](https://github.com/hyperledger-identus/cloud-agent/issues/1203)) ([9050094](https://github.com/hyperledger-identus/cloud-agent/commit/905009490cf0c360431fa235cf26609bea9b951b))
* ATL-6833 integrate ZIO failures and defects in wallet event controller ([#1186](https://github.com/hyperledger-identus/cloud-agent/issues/1186)) ([8bc2018](https://github.com/hyperledger-identus/cloud-agent/commit/8bc2018bd1ca6d1aa679e1935cde5996602b8ae5))
* ATL-6834 Use ZIO Failures and Defects effectively in the Issue flow ([#1139](https://github.com/hyperledger-identus/cloud-agent/issues/1139)) ([ede7b77](https://github.com/hyperledger-identus/cloud-agent/commit/ede7b770341d1efd5026f7badc1f963ecfefcdef))
* Handle Error in Background Jobs - Improve the way we store errors and defects in DB ([#1218](https://github.com/hyperledger-identus/cloud-agent/issues/1218)) ([e3cadc9](https://github.com/hyperledger-identus/cloud-agent/commit/e3cadc9eb7f3adde05548345c456d1c059356a1c))
* implement ADR Use ZIO Failures and Defects Effectively - Mercury should not throw exceptions ([#1192](https://github.com/hyperledger-identus/cloud-agent/issues/1192)) ([a4ce87f](https://github.com/hyperledger-identus/cloud-agent/commit/a4ce87fd709102e0a5e597e5ba50891e01d46a51))
* improve OpenAPI's PresentProof to make more consistent ([#1130](https://github.com/hyperledger-identus/cloud-agent/issues/1130)) ([bdc5d20](https://github.com/hyperledger-identus/cloud-agent/commit/bdc5d207a6d1567b9dca88f4d72300de091d1d24))
* Integrate ZIO failures and defects ADR in DID Registrar Controller and Mercury ([#1180](https://github.com/hyperledger-identus/cloud-agent/issues/1180)) ([d8e2120](https://github.com/hyperledger-identus/cloud-agent/commit/d8e21201f2b07cd03afbba8fb668329105048ba2))
* SDJWT holder key binding ([#1185](https://github.com/hyperledger-identus/cloud-agent/issues/1185)) ([628f2f0](https://github.com/hyperledger-identus/cloud-agent/commit/628f2f07b29824a899b2c50b0d9cd1c5449bd0e7))
* use kid in the jwt proof header of OID4VCI CredentialIssue request ([#1184](https://github.com/hyperledger-identus/cloud-agent/issues/1184)) ([ee53eda](https://github.com/hyperledger-identus/cloud-agent/commit/ee53edae1fc0bb7f06770a835234052de46665f5))
* use the compact format in SD-JWT ([#1169](https://github.com/hyperledger-identus/cloud-agent/issues/1169)) ([65da651](https://github.com/hyperledger-identus/cloud-agent/commit/65da65185ebda33cab499e87196bf9ce3543b3aa))

### Bug Fixes

* avoid name and operationId conflict when creating oas client ([#1233](https://github.com/hyperledger-identus/cloud-agent/issues/1233)) ([73e8e24](https://github.com/hyperledger-identus/cloud-agent/commit/73e8e2445e038ea154b0ddd0ca93f15fa9db69b4))
* incorrect parsing of public key coordinates and enable some tests again ([#1215](https://github.com/hyperledger-identus/cloud-agent/issues/1215)) ([5398a75](https://github.com/hyperledger-identus/cloud-agent/commit/5398a75cfde7ee922588bd84608223abf389d5c1))
* KeyID from String to Opaque Type and presentation job cleanup  ([#1190](https://github.com/hyperledger-identus/cloud-agent/issues/1190)) ([b813faf](https://github.com/hyperledger-identus/cloud-agent/commit/b813faf5f076521955d4b84cab5d46d3635159a2))
* make init-script.sh idempotent [#1173](https://github.com/hyperledger-identus/cloud-agent/issues/1173) ([#1194](https://github.com/hyperledger-identus/cloud-agent/issues/1194)) ([1712062](https://github.com/hyperledger-identus/cloud-agent/commit/1712062cafcba4a46f4d190a7ef628208d21ce7b))
* migrate and repair in case of renaming issues ([#1211](https://github.com/hyperledger-identus/cloud-agent/issues/1211)) ([cb4d479](https://github.com/hyperledger-identus/cloud-agent/commit/cb4d47927c4d6f1c02e4fcf95a52e5ce074d2724))
* Present Error Handling (Part 1: Repo Changes) ([#1172](https://github.com/hyperledger-identus/cloud-agent/issues/1172)) ([13e2447](https://github.com/hyperledger-identus/cloud-agent/commit/13e244738c54bcc43bbea694acebc9b372a1ccb5))
* Present Error Handling Job ([#1204](https://github.com/hyperledger-identus/cloud-agent/issues/1204)) ([3191d8b](https://github.com/hyperledger-identus/cloud-agent/commit/3191d8b933e1e7e08ccd5dd95dbcfddb1ae8bb01))
* present error handling Part 2 ([#1177](https://github.com/hyperledger-identus/cloud-agent/issues/1177)) ([9ac6e52](https://github.com/hyperledger-identus/cloud-agent/commit/9ac6e52ec447710ae7917d8176dcb0ebdc6b2e0d))
* Verification Policy Error Handling ([#1228](https://github.com/hyperledger-identus/cloud-agent/issues/1228)) ([6117a3c](https://github.com/hyperledger-identus/cloud-agent/commit/6117a3cc00121c34a06fd680b1a0b4df4b188f49))

## [1.36.1](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.36.0...cloud-agent-v1.36.1) (2024-06-09)

### Bug Fixes

* Helm chart refactor vol2 ([#1162](https://github.com/hyperledger-identus/cloud-agent/issues/1162)) ([72fc6d1](https://github.com/hyperledger-identus/cloud-agent/commit/72fc6d1a61a4a1a5c0b1c81d3b0742538c8b9bc7))

## [1.36.0](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.35.1...cloud-agent-v1.36.0) (2024-06-09)

### Features

* improve ZIO failures and defects in credential definition ([#1133](https://github.com/hyperledger-identus/cloud-agent/issues/1133)) ([d6dfb72](https://github.com/hyperledger-identus/cloud-agent/commit/d6dfb72e298127b3e9d3a4c6577f62f4c8a1970a))
* Remove double Error logs in DIDController ([#1140](https://github.com/hyperledger-identus/cloud-agent/issues/1140)) ([888ebb4](https://github.com/hyperledger-identus/cloud-agent/commit/888ebb4b484224d9c73674cffdf7d00777cfb4dc))

### Bug Fixes

* Credential Defintion Error Handling Part 2 ([#1155](https://github.com/hyperledger-identus/cloud-agent/issues/1155)) ([2df5306](https://github.com/hyperledger-identus/cloud-agent/commit/2df530695522789bed6624bae8bd07433c05ddda))
* Credential Defintion Error Handling Part 2 ([#1156](https://github.com/hyperledger-identus/cloud-agent/issues/1156)) ([5755504](https://github.com/hyperledger-identus/cloud-agent/commit/57555047cd56ae31bbf14601fe8b5d96f838e033))
* Helm chart refactor ([#1160](https://github.com/hyperledger-identus/cloud-agent/issues/1160)) ([4b59112](https://github.com/hyperledger-identus/cloud-agent/commit/4b59112af649a00e17d6e5e0e927f5b972629ec9))
* pick right key type when creating corresponding issuer ([#1157](https://github.com/hyperledger-identus/cloud-agent/issues/1157)) ([22f0448](https://github.com/hyperledger-identus/cloud-agent/commit/22f0448ca878b385eada89d805c8f993c52173a2))
* Schema Error Handling ([#1138](https://github.com/hyperledger-identus/cloud-agent/issues/1138)) ([a9da840](https://github.com/hyperledger-identus/cloud-agent/commit/a9da84098bd56eeb9d64e7b2bdd78d5596cf35c5))
* Update the Holder to send the presentation only, No claims to disclose is needed separately  ([#1158](https://github.com/hyperledger-identus/cloud-agent/issues/1158)) ([9eaa5d4](https://github.com/hyperledger-identus/cloud-agent/commit/9eaa5d444665dbda00038a9032b890afcfa4bc15))

## [1.35.1](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.35.0...cloud-agent-v1.35.1) (2024-06-05)

### Bug Fixes

* Add expiration time for cloud-agent ([#1132](https://github.com/hyperledger-identus/cloud-agent/issues/1132)) ([f719120](https://github.com/hyperledger-identus/cloud-agent/commit/f719120211a83e96e6e4e282cc70e9f860d9298d))
* Chart refactor ([#1143](https://github.com/hyperledger-identus/cloud-agent/issues/1143)) ([f309a0e](https://github.com/hyperledger-identus/cloud-agent/commit/f309a0e7c2101c2567498b060ee71a6964d3b9e6))

## [1.35.0](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.34.0...cloud-agent-v1.35.0) (2024-06-03)

### Features

* **agent:** ATL-6839 migrate DIDComm endpoint to tapir ([#1116](https://github.com/hyperledger-identus/cloud-agent/issues/1116)) ([2f4f7c3](https://github.com/hyperledger-identus/cloud-agent/commit/2f4f7c395523763c3c7066781017430523317841))
* integrate SD JWT ([#1016](https://github.com/hyperledger-identus/cloud-agent/issues/1016)) ([9d7948f](https://github.com/hyperledger-identus/cloud-agent/commit/9d7948fc8208edf9f8c1712a0cd6902474f0814d))

### Bug Fixes

* SemanticCheckOfClaims In Verification API ([#1124](https://github.com/hyperledger-identus/cloud-agent/issues/1124)) ([7cb4192](https://github.com/hyperledger-identus/cloud-agent/commit/7cb4192d41a779e2ba4de815b2e8ec469636e485))
* update the jose dependency and switch back to the official library ([#1117](https://github.com/hyperledger-identus/cloud-agent/issues/1117)) ([3608aaf](https://github.com/hyperledger-identus/cloud-agent/commit/3608aafd980472cb63e5164339681fb079190dba))
* Changing .chart.name reference, adding name override ([#1129](https://github.com/hyperledger-identus/cloud-agent/issues/1129)) ([650ae3b](https://github.com/hyperledger-identus/cloud-agent/commit/650ae3ba94ae322fc745eb07658f1663b3e7e321))
* VC Verification API Doc ([#1118](https://github.com/hyperledger-identus/cloud-agent/issues/1118)) ([d70d4b7](https://github.com/hyperledger-identus/cloud-agent/commit/d70d4b74cb9f21d72e735f9650b746565068f282))

## [1.34.0](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.33.1...cloud-agent-v1.34.0) (2024-05-27)

### Features

* Rename helm chart and its resources ([#1104](https://github.com/hyperledger-identus/cloud-agent/issues/1104)) ([84c5cea](https://github.com/hyperledger-identus/cloud-agent/commit/84c5ceaf97ee491ebcb07b12f2cc78a4da1e2dc1))

## [1.33.1](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.33.0...cloud-agent-v1.33.1) (2024-05-21)

### Bug Fixes

* broken link for the cloud agent packages in readme file ([#1032](https://github.com/hyperledger-identus/cloud-agent/issues/1032)) ([92d17c2](https://github.com/hyperledger-identus/cloud-agent/commit/92d17c2b5f82d0ca35313ab51bd90b6f55d2cd87))
* expose new key types in rest api ([#1066](https://github.com/hyperledger-identus/cloud-agent/issues/1066)) ([9ce8d3a](https://github.com/hyperledger-identus/cloud-agent/commit/9ce8d3a8742f86c9a593c705e0f3aa472ff10987))
* rename the folder to identus for vc-jwt ([#1063](https://github.com/hyperledger-identus/cloud-agent/issues/1063)) ([364a5dc](https://github.com/hyperledger-identus/cloud-agent/commit/364a5dc7eb2b9f23b18f3775c207feff02893cbe))

### Performance Improvements

* update ts client in the performance tests, cleanup `println` ([#1041](https://github.com/hyperledger-identus/cloud-agent/issues/1041)) ([7d5ceba](https://github.com/hyperledger-identus/cloud-agent/commit/7d5cebafb34191964acfdf190c743a2ba253e883))

## [1.33.0](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.32.1...cloud-agent-v1.33.0) (2024-05-09)

### Features

* rename `prism-agent` to `cloud-agent` ([#1019](https://github.com/hyperledger-identus/cloud-agent/issues/1019)) ([74560da](https://github.com/hyperledger-identus/cloud-agent/commit/74560dabf59dac15ccd086edb7a77d9e5055621e))

### Bug Fixes

* integration test ([#1011](https://github.com/hyperledger-identus/cloud-agent/issues/1011)) ([d674f31](https://github.com/hyperledger-identus/cloud-agent/commit/d674f3162be44ba05d50b305be4838525d982706))

## [1.32.1](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.32.0...cloud-agent-v1.32.1) (2024-05-07)

### Bug Fixes

* expose pg_admin port on the localhost interface only ([#957](https://github.com/hyperledger-identus/cloud-agent/issues/957)) ([73674b5](https://github.com/hyperledger-identus/cloud-agent/commit/73674b5da6a41c4972ac3c45005ce768608b558e))
* Fix OneOf OpenAPI Serialization Issue ([#1010](https://github.com/hyperledger-identus/cloud-agent/issues/1010)) ([393c296](https://github.com/hyperledger-identus/cloud-agent/commit/393c29654b8d3d53071f0d2932a16ff81688ece6))
* remove prism-crypto dependency ([#1015](https://github.com/hyperledger-identus/cloud-agent/issues/1015)) ([46e594c](https://github.com/hyperledger-identus/cloud-agent/commit/46e594c21bdb43d78f41be6c803ad8b80dc89504))
* update open-api-spec and generator script and package.json ([#990](https://github.com/hyperledger-identus/cloud-agent/issues/990)) ([88c1b5e](https://github.com/hyperledger-identus/cloud-agent/commit/88c1b5eadf62ad0efcd4ee53b793bb08cce9667f))

## [1.32.0](https://github.com/hyperledger-identus/cloud-agent/compare/cloud-agent-v1.31.0...cloud-agent-v1.32.0) (2024-04-26)

### Features

* add sample maintainers.md ([#878](https://github.com/hyperledger-identus/cloud-agent/issues/878)) ([c6a41ed](https://github.com/hyperledger-identus/cloud-agent/commit/c6a41edc5466312da76283d37f12406d79449a7d))
* **agent:** verification API integration test + documentation ATL-6775 ([#986](https://github.com/hyperledger-identus/cloud-agent/issues/986)) ([9308b21](https://github.com/hyperledger-identus/cloud-agent/commit/9308b21e46d267265c7fcdbfc88b78ddd5ae6558))
* Align the repo with new name identus-cloud-agent ([#973](https://github.com/hyperledger-identus/cloud-agent/issues/973)) ([9fc7bb0](https://github.com/hyperledger-identus/cloud-agent/commit/9fc7bb07cac9aae1db8d389ec8831403f106d612))
* Configurations load improvement ([#954](https://github.com/hyperledger-identus/cloud-agent/issues/954)) ([dfb7577](https://github.com/hyperledger-identus/cloud-agent/commit/dfb75778f925a615aaec815241d964014d236777))
* **connect:** ATL-6599 Use ZIO Failures and Defects effectively + RFC-9457 in connect ([#927](https://github.com/hyperledger-identus/cloud-agent/issues/927)) ([eb898e0](https://github.com/hyperledger-identus/cloud-agent/commit/eb898e068f768507d6979a5d9bab35ef7ad4a045))
* key management for Ed25519 and X25519 ([#966](https://github.com/hyperledger-identus/cloud-agent/issues/966)) ([a0f6819](https://github.com/hyperledger-identus/cloud-agent/commit/a0f6819bb80d87c13f903d7dc1b67cb08b4687db))
* Vc Verification Api ([#975](https://github.com/hyperledger-identus/cloud-agent/issues/975)) ([f0a1f2c](https://github.com/hyperledger-identus/cloud-agent/commit/f0a1f2c1aaafb636cabcb84c599d9deaa90fd373))

### Bug Fixes

* add shared-crypto module and apollo wrapper for other key types ([#958](https://github.com/hyperledger-identus/cloud-agent/issues/958)) ([7eaa66c](https://github.com/hyperledger-identus/cloud-agent/commit/7eaa66c51904e58c309fd3bb0a8e8864c7902cb9))
* Check purpose of the keys ([#968](https://github.com/hyperledger-identus/cloud-agent/issues/968)) ([4b8e48d](https://github.com/hyperledger-identus/cloud-agent/commit/4b8e48d238b751ee8526e1440e8a0515f6b62de5))
* Integration Test ([#974](https://github.com/hyperledger-identus/cloud-agent/issues/974)) ([847eb2f](https://github.com/hyperledger-identus/cloud-agent/commit/847eb2f56ba2766ed28f6484391cbd9b3202fbe5))
* **prism-agent:** add missing 'PresentationVerificationFailed' status to REST API response enum ([#948](https://github.com/hyperledger-identus/cloud-agent/issues/948)) ([9a38cc9](https://github.com/hyperledger-identus/cloud-agent/commit/9a38cc97c18d26f13efa1c9535de1df003547a2b))
* use apollo for secp256k1 in shared-crypto ([#971](https://github.com/hyperledger-identus/cloud-agent/issues/971)) ([dd5e20b](https://github.com/hyperledger-identus/cloud-agent/commit/dd5e20bdedd004c2e0a81383d10b5eee3ae03788))

## [1.31.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.30.1...prism-agent-v1.31.0) (2024-03-20)

### Features

* add revocation for JWT credentials ([#934](https://github.com/hyperledger-identus/cloud-agent/issues/934)) ([88b7fa5](https://github.com/hyperledger-identus/cloud-agent/commit/88b7fa5c6cd92002ef355311eec6e30b63ab1dd6))
* Fix Update Schema and CredentialDef on Receive Credential ([#920](https://github.com/hyperledger-identus/cloud-agent/issues/920)) ([acbba3c](https://github.com/hyperledger-identus/cloud-agent/commit/acbba3ce92ee9e16893b4978c4a9ec4ce0757d53))

### Bug Fixes

* add Anoncreds Integration Test ([#923](https://github.com/hyperledger-identus/cloud-agent/issues/923)) ([27a157f](https://github.com/hyperledger-identus/cloud-agent/commit/27a157fb5a83a33704230b35eca80f297eec9a85))
* align keycloak version ([#936](https://github.com/hyperledger-identus/cloud-agent/issues/936)) ([c920fd6](https://github.com/hyperledger-identus/cloud-agent/commit/c920fd661d31b56466497e462fd0139755290b33))
* anoncred test ([#940](https://github.com/hyperledger-identus/cloud-agent/issues/940)) ([bb5ead1](https://github.com/hyperledger-identus/cloud-agent/commit/bb5ead1de3b6d2c3fae0019d50a5310b37a2eab3))
* **pollux:** function that allocates status list credential does not work correctly in multi threaded environment  ([#941](https://github.com/hyperledger-identus/cloud-agent/issues/941)) ([ecc3c01](https://github.com/hyperledger-identus/cloud-agent/commit/ecc3c019749a6b370771bcf62f89ea2599a521ac))
* **pollux:** Undo edit migration for revocation status lists ([#937](https://github.com/hyperledger-identus/cloud-agent/issues/937)) ([7f7585f](https://github.com/hyperledger-identus/cloud-agent/commit/7f7585f47ee197db162c75f7d118a87b99cc7d06))
* **prism-agent:** add validation for endpoint url ([#919](https://github.com/hyperledger-identus/cloud-agent/issues/919)) ([0402a87](https://github.com/hyperledger-identus/cloud-agent/commit/0402a8778eda839521c55a127934fba41c7b79ad))

## [1.30.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.30.0...prism-agent-v1.30.1) (2024-03-06)

### Bug Fixes

* allow configurable path convention for vault secrets ([#918](https://github.com/hyperledger-identus/cloud-agent/issues/918)) ([234a272](https://github.com/hyperledger-identus/cloud-agent/commit/234a2725614b05466391894f248c7175fb62c5b6))
* integration test ([#915](https://github.com/hyperledger-identus/cloud-agent/issues/915)) ([320ab6a](https://github.com/hyperledger-identus/cloud-agent/commit/320ab6a876606eb68f48fe7d78983b4e044b5084))

## [1.30.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.29.0...prism-agent-v1.30.0) (2024-03-01)

### Features

* **agent:** make the connection pool size configurable, fixes [#913](https://github.com/hyperledger-identus/cloud-agent/issues/913) ([#914](https://github.com/hyperledger-identus/cloud-agent/issues/914)) ([375fe0f](https://github.com/hyperledger-identus/cloud-agent/commit/375fe0f8ee042246aed37f40cbeb8f2042c99958))

## [1.29.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.28.0...prism-agent-v1.29.0) (2024-02-28)

### Features

* ZKP verification ([#792](https://github.com/hyperledger-identus/cloud-agent/issues/792)) ([ab1ab64](https://github.com/hyperledger-identus/cloud-agent/commit/ab1ab648b10a82b39d4bdde6e2c9693c8f7506b8))

### Bug Fixes

* correct the config environment variable name ([#905](https://github.com/hyperledger-identus/cloud-agent/issues/905)) ([d86436c](https://github.com/hyperledger-identus/cloud-agent/commit/d86436cbc58571b4167411643623f3ba975550ad))

## [1.28.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.27.0...prism-agent-v1.28.0) (2024-02-20)

### Features

* add credential def performance test ([#865](https://github.com/hyperledger-identus/cloud-agent/issues/865)) ([95064d6](https://github.com/hyperledger-identus/cloud-agent/commit/95064d617dda7d45916fbdddf20a544eea4acf4a))
* **pollux:** add Json VC schema meta validation ([#892](https://github.com/hyperledger-identus/cloud-agent/issues/892)) ([19c42b1](https://github.com/hyperledger-identus/cloud-agent/commit/19c42b10188d1e0242bdceb1f89b6410dcc05353))

### Bug Fixes

* **prism-agent:** increase http timeout communication channel closing… ([#901](https://github.com/hyperledger-identus/cloud-agent/issues/901)) ([8d3f29d](https://github.com/hyperledger-identus/cloud-agent/commit/8d3f29ddd830fe102d4bf25a0af8734730c80151))
* re-enable logging with SLF4J and add traceId ([#869](https://github.com/hyperledger-identus/cloud-agent/issues/869)) ([8f6af25](https://github.com/hyperledger-identus/cloud-agent/commit/8f6af25a8eafd27d5017096da64f89188354a2ca))
* remove oas schema format for empty repsonse body ([#902](https://github.com/hyperledger-identus/cloud-agent/issues/902)) ([5f2bb08](https://github.com/hyperledger-identus/cloud-agent/commit/5f2bb0872a156c9223ab56efbd47e812967ff582))

## [1.27.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.26.0...prism-agent-v1.27.0) (2024-02-08)

### Features

* support vault AppRole authentication ([#884](https://github.com/hyperledger-identus/cloud-agent/issues/884)) ([441f878](https://github.com/hyperledger-identus/cloud-agent/commit/441f878f0b573f350ffed14de0df164e1e260122))

### Bug Fixes

* remove hard code did:example:* ([#882](https://github.com/hyperledger-identus/cloud-agent/issues/882)) ([321faf5](https://github.com/hyperledger-identus/cloud-agent/commit/321faf5791b05f0c24dd6ed96f155aa65d06477d))

## [1.26.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.25.0...prism-agent-v1.26.0) (2024-02-06)

### Features

* interoperable schema changes ([#870](https://github.com/hyperledger-identus/cloud-agent/issues/870)) ([de49a93](https://github.com/hyperledger-identus/cloud-agent/commit/de49a9328524b32d714301f2b7961d5cd85b23c3))

### Bug Fixes

* **prism-agent:**  update didcomm peerdid library to support latest spec ([#877](https://github.com/hyperledger-identus/cloud-agent/issues/877)) ([0c42a62](https://github.com/hyperledger-identus/cloud-agent/commit/0c42a622143e35c439ae83cc3bb746515ce7401b))

## [1.25.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.24.0...prism-agent-v1.25.0) (2024-01-25)

### Features

* **prism-agent:** add JWT auth support for agent-admin role ([#840](https://github.com/hyperledger-identus/cloud-agent/issues/840)) ([3ccd56e](https://github.com/hyperledger-identus/cloud-agent/commit/3ccd56efadbbb3ea70e7ca63e9eb89564a83c02f))
* upgrade ZIO http client to improve performance ([#850](https://github.com/hyperledger-identus/cloud-agent/issues/850)) ([7aa9b4c](https://github.com/hyperledger-identus/cloud-agent/commit/7aa9b4c27f92b169c72a68cd4bb8f4afb63943d4))

## [1.24.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.23.0...prism-agent-v1.24.0) (2023-12-21)

### Features

* add configuration for gRPC usePlainText (enable TLS for gRPC) ([#823](https://github.com/hyperledger-identus/cloud-agent/issues/823)) ([b871bb5](https://github.com/hyperledger-identus/cloud-agent/commit/b871bb5e8eeeb71b3f22c38609ae8f1ff424016c))

## [1.23.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.22.0...prism-agent-v1.23.0) (2023-12-20)

### Features

* complete the integration with anoncred and fixes ([#820](https://github.com/hyperledger-identus/cloud-agent/issues/820)) ([15ff710](https://github.com/hyperledger-identus/cloud-agent/commit/15ff710c4a68c5f282e07b23098e362825fdb3b9))
* Liveness, and readiness probes ([#817](https://github.com/hyperledger-identus/cloud-agent/issues/817)) ([6e18666](https://github.com/hyperledger-identus/cloud-agent/commit/6e18666f51cca2d0f151b070c59049bcf005a450))
* Tune postgresql - helm chart ([#822](https://github.com/hyperledger-identus/cloud-agent/issues/822)) ([5fcd9fb](https://github.com/hyperledger-identus/cloud-agent/commit/5fcd9fb6301a5c7b1296d60e4b6fa3385ffe6727))

### Bug Fixes

* Change resource defaults for postgres ([#827](https://github.com/hyperledger-identus/cloud-agent/issues/827)) ([87809c4](https://github.com/hyperledger-identus/cloud-agent/commit/87809c4b4d6a3baf0afa37a2cf7ddf6c41a80eb6))
* correct OAS example ([#816](https://github.com/hyperledger-identus/cloud-agent/issues/816)) ([b1384b3](https://github.com/hyperledger-identus/cloud-agent/commit/b1384b38524060f3cfa6df39afaddcce26a5514e))
* Swithing to startupProbe from Readiness ([#821](https://github.com/hyperledger-identus/cloud-agent/issues/821)) ([22a78ec](https://github.com/hyperledger-identus/cloud-agent/commit/22a78ec09ccc84b5d5c03e8f07ff3a14c654cf2b))

## [1.22.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.21.1...prism-agent-v1.22.0) (2023-12-14)

### Features

* Consumer restricition parametarization  ([#814](https://github.com/hyperledger-identus/cloud-agent/issues/814)) ([e039576](https://github.com/hyperledger-identus/cloud-agent/commit/e039576fc0e285b80b2966c032ed91b9a8f26f60))

### Bug Fixes

* correct OAS examples ([#810](https://github.com/hyperledger-identus/cloud-agent/issues/810)) ([a0720dc](https://github.com/hyperledger-identus/cloud-agent/commit/a0720dcbaf10370dcacc1b5102df13929b40dfdb))

## [1.21.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.21.0...prism-agent-v1.21.1) (2023-12-12)

### Bug Fixes

* Option to disable apisix key auth ([#813](https://github.com/hyperledger-identus/cloud-agent/issues/813)) ([f163682](https://github.com/hyperledger-identus/cloud-agent/commit/f1636824047c0d03ce0790ede54e3a12d63dd787))

## [1.21.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.20.1...prism-agent-v1.21.0) (2023-12-12)

### Features

* env vars support through values file ([#811](https://github.com/hyperledger-identus/cloud-agent/issues/811)) ([2486dde](https://github.com/hyperledger-identus/cloud-agent/commit/2486dde9b0682504a02ad031b3e7498b2fa2ce17))

## [1.20.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.20.0...prism-agent-v1.20.1) (2023-12-06)

### Bug Fixes

* tolerations and nodeAffinity for k8s ([#808](https://github.com/hyperledger-identus/cloud-agent/issues/808)) ([7934fa4](https://github.com/hyperledger-identus/cloud-agent/commit/7934fa402ba86af6d8430208f1844fbd6ccda1bd))

## [1.20.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.19.1...prism-agent-v1.20.0) (2023-12-05)

### Features

* add nodeAffinity, tolerations, and resources to k8s deployment ([#804](https://github.com/hyperledger-identus/cloud-agent/issues/804)) ([22407a3](https://github.com/hyperledger-identus/cloud-agent/commit/22407a3103eff73d87ead9a8122f078845c11d95))

## [1.19.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.19.0...prism-agent-v1.19.1) (2023-11-29)

### Bug Fixes

* change admin auth priority and improve auth error message ([#800](https://github.com/hyperledger-identus/cloud-agent/issues/800)) ([32d4340](https://github.com/hyperledger-identus/cloud-agent/commit/32d43401a69c339f54380bd8d5dfe2fa383cb8d7))

## [1.19.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.18.0...prism-agent-v1.19.0) (2023-11-21)

### Features

* Accept goal and goalCode to create connection invitation  ([#785](https://github.com/hyperledger-identus/cloud-agent/issues/785)) ([71c776b](https://github.com/hyperledger-identus/cloud-agent/commit/71c776baa2caf3ca610508dba805f037fd7d6e29))
* **docs:** ADR for revocation status list expansion strategy ([#773](https://github.com/hyperledger-identus/cloud-agent/issues/773)) ([7ad6427](https://github.com/hyperledger-identus/cloud-agent/commit/7ad64277acb2bffe12524c4bfb68f687689b5b2e))
* Keycloak container support with clients and PermissionManagement service ([#755](https://github.com/hyperledger-identus/cloud-agent/issues/755)) ([a1846aa](https://github.com/hyperledger-identus/cloud-agent/commit/a1846aaa84202b55d48ea8556aad8cbbb8260f4d))
* **pollux:** Add migrations needed for JWT revocation ([#778](https://github.com/hyperledger-identus/cloud-agent/issues/778)) ([471956e](https://github.com/hyperledger-identus/cloud-agent/commit/471956e92893a7237cabca2fb065adb417678d37))
* **prism-agent:** add multi-tenant wallet self-service capability ([#779](https://github.com/hyperledger-identus/cloud-agent/issues/779)) ([f2e74cd](https://github.com/hyperledger-identus/cloud-agent/commit/f2e74cd1957e7d76f6dccadd02b1ca5b794d02b1))

### Bug Fixes

* check for active RLS on db application user ([#775](https://github.com/hyperledger-identus/cloud-agent/issues/775)) ([a792f43](https://github.com/hyperledger-identus/cloud-agent/commit/a792f43eaae0ec2cd30db2ea3308deded7a1a935))
* enable keycloak with pre-configured agent in helm chart ([#791](https://github.com/hyperledger-identus/cloud-agent/issues/791)) ([9a6e512](https://github.com/hyperledger-identus/cloud-agent/commit/9a6e5123e07462db66017439e8e434315af7c0f4))
* explicitly define transitive dependencies that were unresolvable ([#790](https://github.com/hyperledger-identus/cloud-agent/issues/790)) ([0647829](https://github.com/hyperledger-identus/cloud-agent/commit/0647829af813913aebd0dd3d703db7e363d44369))
* **pollux:** V16 migration is failing to add FK constraint because of type mismatch ([#782](https://github.com/hyperledger-identus/cloud-agent/issues/782)) ([c87beb0](https://github.com/hyperledger-identus/cloud-agent/commit/c87beb0478d4b3d54709e09597c42c23878d101e))
* **prism-agent:** more descriptive error response for validateDID in issue flow ([#783](https://github.com/hyperledger-identus/cloud-agent/issues/783)) ([b99a737](https://github.com/hyperledger-identus/cloud-agent/commit/b99a73718a06f4b97d933ba2e3220593f8d4e825))
* **prism-agent:** perform percent encoding on auth header for token introspection request ([#780](https://github.com/hyperledger-identus/cloud-agent/issues/780)) ([03d43c9](https://github.com/hyperledger-identus/cloud-agent/commit/03d43c98d8ab64e5b47830d95a6356f9d6dd1b82))

## [1.18.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.17.0...prism-agent-v1.18.0) (2023-10-24)

### Features

* presentation API refactor ([#765](https://github.com/hyperledger-identus/cloud-agent/issues/765)) ([045d829](https://github.com/hyperledger-identus/cloud-agent/commit/045d8298f8865baeb13e243ed058e8e440b3f496))
* add new auth params ([#762](https://github.com/hyperledger-identus/cloud-agent/issues/762)) ([b8bfb86](https://github.com/hyperledger-identus/cloud-agent/commit/b8bfb867061c58fc12987b5405f561e8f10cb718))
* disable cors by default ([#747](https://github.com/hyperledger-identus/cloud-agent/issues/747)) ([1dd8c8b](https://github.com/hyperledger-identus/cloud-agent/commit/1dd8c8b0e9b0d2593bd1c17a95bf013192a64532))
* migrate docker image of the agent to Java 21 ([#758](https://github.com/hyperledger-identus/cloud-agent/issues/758)) ([d36dbf0](https://github.com/hyperledger-identus/cloud-agent/commit/d36dbf0dfbf45b64185e5b54aba0444d6e1ada88))
* **prism-agent:** add keycloak authorization support to endpoints ([#753](https://github.com/hyperledger-identus/cloud-agent/issues/753)) ([3e7534f](https://github.com/hyperledger-identus/cloud-agent/commit/3e7534ff1a75e9ecaa0c2b670c1c158890021f8d))

### Bug Fixes

* all performance tests run succesfully, add group thresholds ([#750](https://github.com/hyperledger-identus/cloud-agent/issues/750)) ([5204838](https://github.com/hyperledger-identus/cloud-agent/commit/520483836e5b572e8aeeecd28f4bbe7cc668c3d9))

## [1.17.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.16.4...prism-agent-v1.17.0) (2023-10-14)

### Features

* **prism-agent:** check issuing DID validity when creating a VC offer + return 'metaRetries' ([#740](https://github.com/hyperledger-identus/cloud-agent/issues/740)) ([f2e2fd3](https://github.com/hyperledger-identus/cloud-agent/commit/f2e2fd3d0397422be40b11644f8b84ddd3c6985f))
* **prism-agent:** implement AnonCreds issuance flow ([#693](https://github.com/hyperledger-identus/cloud-agent/issues/693)) ([9165a6f](https://github.com/hyperledger-identus/cloud-agent/commit/9165a6f8fc0a11bd6c19b0bfd4dd4217ea3194d9))

### Bug Fixes

* change repository and name for rest api clients ([#745](https://github.com/hyperledger-identus/cloud-agent/issues/745)) ([0f84e28](https://github.com/hyperledger-identus/cloud-agent/commit/0f84e28c3f2800c1d73353e40620a840fbb6b93a))
* improve performance for background jobs in multitenancy mode ([#749](https://github.com/hyperledger-identus/cloud-agent/issues/749)) ([17def3f](https://github.com/hyperledger-identus/cloud-agent/commit/17def3f67c1eb687560aee844aba6ff2a0bd4137))
* **prism-agent:** agent should read DIDComm port from config ([#757](https://github.com/hyperledger-identus/cloud-agent/issues/757)) ([cda908c](https://github.com/hyperledger-identus/cloud-agent/commit/cda908c87cee562e6c044aa405aa82bd510cc74e))
* **prism-agent:** configure APISIX to return CORS headers from Prism Agent endpoints ([#746](https://github.com/hyperledger-identus/cloud-agent/issues/746)) ([a579aa9](https://github.com/hyperledger-identus/cloud-agent/commit/a579aa95ea5c0c4950cb64b8b9adb1f56bb87eb2))
* **prism-agent:** fix docker env variables interpolation issue ([#751](https://github.com/hyperledger-identus/cloud-agent/issues/751)) ([110eb2d](https://github.com/hyperledger-identus/cloud-agent/commit/110eb2df9590412b35997152d526c599edb8e7af))
* **prism-agent:** return relevant errors on offer creation ([#754](https://github.com/hyperledger-identus/cloud-agent/issues/754)) ([d36533f](https://github.com/hyperledger-identus/cloud-agent/commit/d36533fe538812c9e3647bcc2383700173e4b1b7))
* prohibit tenants to use equal or revoked api keys ([#742](https://github.com/hyperledger-identus/cloud-agent/issues/742)) ([4b10c3a](https://github.com/hyperledger-identus/cloud-agent/commit/4b10c3af931722a683bf55062297c3dfa1e38046))
* upgrade vault and quill versions ([#739](https://github.com/hyperledger-identus/cloud-agent/issues/739)) ([c140857](https://github.com/hyperledger-identus/cloud-agent/commit/c140857df97d56ab750ec186962e5fe2bb6a6717))

## [1.16.4](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.16.3...prism-agent-v1.16.4) (2023-09-29)

### Bug Fixes

* Integration flow ATL-5777 ([#738](https://github.com/hyperledger-identus/cloud-agent/issues/738)) ([7cf927c](https://github.com/hyperledger-identus/cloud-agent/commit/7cf927cc267460cc1708e424c0a63ae96689a42a))

## [1.16.3](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.16.2...prism-agent-v1.16.3) (2023-09-28)

### Bug Fixes

* Adding labels [skip ci] ([#737](https://github.com/hyperledger-identus/cloud-agent/issues/737)) ([5182098](https://github.com/hyperledger-identus/cloud-agent/commit/5182098f7bc6db479ee64d7133389732a38d174a))

## [1.16.2](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.16.1...prism-agent-v1.16.2) (2023-09-28)

### Bug Fixes

* Changing yq command [skip ci] ATL-5777 ([#736](https://github.com/hyperledger-identus/cloud-agent/issues/736)) ([01bdfa7](https://github.com/hyperledger-identus/cloud-agent/commit/01bdfa74056d983bc1fd494c99b1bab8496dc62f))
* Integration flow ([#734](https://github.com/hyperledger-identus/cloud-agent/issues/734)) ([c45a9eb](https://github.com/hyperledger-identus/cloud-agent/commit/c45a9ebf0150245dfa5ebdc2eda94aa1e0fea8f3))
* Renaming values.yml [skip ci] ATL-5777  ([#735](https://github.com/hyperledger-identus/cloud-agent/issues/735)) ([bcd73c3](https://github.com/hyperledger-identus/cloud-agent/commit/bcd73c310a1c033400f83cfa266cbe0aa304217a))

## [1.16.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.16.0...prism-agent-v1.16.1) (2023-09-27)

### Bug Fixes

* Adding localhost environment variable in run.sh script for local development ([#728](https://github.com/hyperledger-identus/cloud-agent/issues/728)) ([1a904a6](https://github.com/hyperledger-identus/cloud-agent/commit/1a904a6f72676fb89f87dd2da14c01d291371f8c))
* correct typo on sts header (dmax -> max) ([#726](https://github.com/hyperledger-identus/cloud-agent/issues/726)) ([2c5bc51](https://github.com/hyperledger-identus/cloud-agent/commit/2c5bc51fc66b2c62a7c8ba7e25944704c335253f))
* **prism-agent:** introduce generic secret store for CD ([#727](https://github.com/hyperledger-identus/cloud-agent/issues/727)) ([3d4aacd](https://github.com/hyperledger-identus/cloud-agent/commit/3d4aacdd9a7f66f2f656d3c31b3f8202cc37c51b))
* Separate config for integration flow ATL-5777 ([#731](https://github.com/hyperledger-identus/cloud-agent/issues/731)) ([9e0e2de](https://github.com/hyperledger-identus/cloud-agent/commit/9e0e2de77a25166f019f78356b2e98b60da7b3e1))
* Separate config for integration flow ATL-5777 ([#733](https://github.com/hyperledger-identus/cloud-agent/issues/733)) ([8380ccc](https://github.com/hyperledger-identus/cloud-agent/commit/8380cccea0eee17c090928b1ae36b877a822177d))

## [1.16.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.15.0...prism-agent-v1.16.0) (2023-09-15)

### Features

* **prism-agent:** Metrics for verification flow ([#714](https://github.com/hyperledger-identus/cloud-agent/issues/714)) ([8bea26e](https://github.com/hyperledger-identus/cloud-agent/commit/8bea26e955987e1543984e090bedad17a7863268))

### Bug Fixes

* change attribute for appuser to login ([#721](https://github.com/hyperledger-identus/cloud-agent/issues/721)) ([a0e0a74](https://github.com/hyperledger-identus/cloud-agent/commit/a0e0a7412172a7cc2010c39c8ee106319e710986))
* entity create and update operation failures if the walletId does… ([#718](https://github.com/hyperledger-identus/cloud-agent/issues/718)) ([4fe6677](https://github.com/hyperledger-identus/cloud-agent/commit/4fe66773a5aad4dc2808dad036c54c4660b3a855))
* **prism-agent:** define db app user privileges before app starts ([#722](https://github.com/hyperledger-identus/cloud-agent/issues/722)) ([8039654](https://github.com/hyperledger-identus/cloud-agent/commit/803965482e2634d488d2f4f364b041917be514a5))
* **prism-agent:** incorrect present proof metric name and remove connectionID from flow metrics ([#720](https://github.com/hyperledger-identus/cloud-agent/issues/720)) ([52e31b0](https://github.com/hyperledger-identus/cloud-agent/commit/52e31b0721d959fa53c8c49a39288b7c50d4582d))
* **prism-agent:** refine multi-tenant error response and validations ([#719](https://github.com/hyperledger-identus/cloud-agent/issues/719)) ([1f9ede3](https://github.com/hyperledger-identus/cloud-agent/commit/1f9ede395c4469bf26b167a6430ad42ea7cde301))
* **prism-agent:** validate application config during startup ([#712](https://github.com/hyperledger-identus/cloud-agent/issues/712)) ([46fd69b](https://github.com/hyperledger-identus/cloud-agent/commit/46fd69bc2416c72dd457b29f06dd181cf65f52a0))
* use postgres application user ([#717](https://github.com/hyperledger-identus/cloud-agent/issues/717)) ([63403a5](https://github.com/hyperledger-identus/cloud-agent/commit/63403a5d64860d4683ebaab00a86eec0578a21c0))

## [1.15.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.14.2...prism-agent-v1.15.0) (2023-09-12)

### Features

* add security headers in helm-chart apisix route ([#697](https://github.com/hyperledger-identus/cloud-agent/issues/697)) ([7f7e0a4](https://github.com/hyperledger-identus/cloud-agent/commit/7f7e0a4b7709c9eb0dbfc0557ed68648a98e5756))
* **prism-agent:** add multi-tenancy capability ([#696](https://github.com/hyperledger-identus/cloud-agent/issues/696)) ([b6c9a40](https://github.com/hyperledger-identus/cloud-agent/commit/b6c9a40733af1a80c2fc7c17650d1f9ca53c21da))

## [1.14.2](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.14.1...prism-agent-v1.14.2) (2023-09-06)

### Bug Fixes

* add missing leading '/' in rewrite rule target ([#692](https://github.com/hyperledger-identus/cloud-agent/issues/692)) ([f2be228](https://github.com/hyperledger-identus/cloud-agent/commit/f2be22895c893525b50a0848d2951e668c5fe688))

## [1.14.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.14.0...prism-agent-v1.14.1) (2023-09-06)

### Bug Fixes

* APISIX config route and rule names too long ([#691](https://github.com/hyperledger-identus/cloud-agent/issues/691)) ([bef008e](https://github.com/hyperledger-identus/cloud-agent/commit/bef008ecb8f07a7e92cba1ba009a343af7a71adb))

## [1.14.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.13.0...prism-agent-v1.14.0) (2023-09-06)

### Features

* **prism-agent:** add syncwave to certificate to fix race condition with tls ([#686](https://github.com/hyperledger-identus/cloud-agent/issues/686)) ([854dcf9](https://github.com/hyperledger-identus/cloud-agent/commit/854dcf96c48defcb1f44062c3dfd88555dcaebe1))

### Bug Fixes

* change private sdk 1.4 to public maven ([#685](https://github.com/hyperledger-identus/cloud-agent/issues/685)) ([128bcac](https://github.com/hyperledger-identus/cloud-agent/commit/128bcac5b7006b485ea3dc9272fde29d159f1a03))
* **prism-agent:** update invitation expiration on connection request ([#687](https://github.com/hyperledger-identus/cloud-agent/issues/687)) ([1a1702f](https://github.com/hyperledger-identus/cloud-agent/commit/1a1702fc4e62b4a03a4e4ee32ac7419ea67a4ea1))

## [1.13.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.12.0...prism-agent-v1.13.0) (2023-09-06)

### Features

* **prism-agent:** metrics for issuance flow ([#669](https://github.com/hyperledger-identus/cloud-agent/issues/669)) ([20315ae](https://github.com/hyperledger-identus/cloud-agent/commit/20315aedea3c8c2953cfd5ee391feb10fbc1146c))

### Bug Fixes

* another indentation in apisixroute ([#683](https://github.com/hyperledger-identus/cloud-agent/issues/683)) ([d7c5e52](https://github.com/hyperledger-identus/cloud-agent/commit/d7c5e526c2b40897b3e26a8272468f21fc1dd81f))
* correct vault path ([#678](https://github.com/hyperledger-identus/cloud-agent/issues/678)) ([9426e7f](https://github.com/hyperledger-identus/cloud-agent/commit/9426e7f069e2dd72c18b1e09f7b34b2d37854771))
* CredentialOffer not following spec ([#569](https://github.com/hyperledger-identus/cloud-agent/issues/569)) ([3d479b9](https://github.com/hyperledger-identus/cloud-agent/commit/3d479b9fc0c0bdb0aa78b8c4e2edc8d287a0b6d9))
* indentation in apisixroute ([#682](https://github.com/hyperledger-identus/cloud-agent/issues/682)) ([6eec8ba](https://github.com/hyperledger-identus/cloud-agent/commit/6eec8ba6a32ce088eaaeafe953c4ff554d4765ab))
* **prism-agent:** fix added the InvitationGenerated state to the conn… ([#684](https://github.com/hyperledger-identus/cloud-agent/issues/684)) ([7fdffe3](https://github.com/hyperledger-identus/cloud-agent/commit/7fdffe3990ea08bd6dad5f9d2124146cb8efcff4))
* **prism-agent:** make resolve did representation content type work ([#679](https://github.com/hyperledger-identus/cloud-agent/issues/679)) ([fd417d9](https://github.com/hyperledger-identus/cloud-agent/commit/fd417d9bdac0db98bc3de7a84e4d3277aef3c403))

## [1.12.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.11.0...prism-agent-v1.12.0) (2023-08-31)

### Features

* add anoncreds credential definition rest api ([#624](https://github.com/hyperledger-identus/cloud-agent/issues/624)) ([99e338a](https://github.com/hyperledger-identus/cloud-agent/commit/99e338af6dc1ab2b4b42f4b1bee2a917ccb77b4c))
* allow external API keys to be defined for an agent ([#643](https://github.com/hyperledger-identus/cloud-agent/issues/643)) ([756dea7](https://github.com/hyperledger-identus/cloud-agent/commit/756dea707b1ced9de800cdabfded6dfc100e340e))
* ATL-5571 Generalized Vault to Store Json ([#650](https://github.com/hyperledger-identus/cloud-agent/issues/650)) ([ebf0328](https://github.com/hyperledger-identus/cloud-agent/commit/ebf0328cfb5107954766fe93ffc6b42f4e5a4cb0))
* ATL-5574 Prime Anoncred Lib ([#652](https://github.com/hyperledger-identus/cloud-agent/issues/652)) ([70b2f16](https://github.com/hyperledger-identus/cloud-agent/commit/70b2f16beecdef7eeeabb18f1b25244046ba5a65))
* ATL-5575 Generalize and Streamline Json Schema SerDes logic ([#653](https://github.com/hyperledger-identus/cloud-agent/issues/653)) ([eb4f8f4](https://github.com/hyperledger-identus/cloud-agent/commit/eb4f8f488bcef421e20f770669dfff99f4c1dd98))

### Bug Fixes

* **prism-agent:** invitation expiry configuration and new state ([#655](https://github.com/hyperledger-identus/cloud-agent/issues/655)) ([c61999d](https://github.com/hyperledger-identus/cloud-agent/commit/c61999dd2a256401c30d29b842f0092f4968c6ed))

## [1.11.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.10.0...prism-agent-v1.11.0) (2023-08-21)

### Features

* **prism-agent:** Add prism agent record processing pipeline parameters ([#626](https://github.com/hyperledger-identus/cloud-agent/issues/626)) ([434bdac](https://github.com/hyperledger-identus/cloud-agent/commit/434bdacfc10b854b77bde0c8c7add613d8ee9025))

### Bug Fixes

* **prism-agenet:** Remove connection ID from metrics in connection flow ([#635](https://github.com/hyperledger-identus/cloud-agent/issues/635)) ([515f92f](https://github.com/hyperledger-identus/cloud-agent/commit/515f92f67f6ccd9ae5414d1324ebb769c43d5017))

## [1.10.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.9.2...prism-agent-v1.10.0) (2023-08-16)

### Features

* **agent:** improve OAS spec and refactor DidCommHttpServer code ([#615](https://github.com/hyperledger-identus/cloud-agent/issues/615)) ([301fbab](https://github.com/hyperledger-identus/cloud-agent/commit/301fbabac6c743130c46572056d9b8848a166be1))
* **prism-agent:** Metrics for connection flow job ([#611](https://github.com/hyperledger-identus/cloud-agent/issues/611)) ([695d661](https://github.com/hyperledger-identus/cloud-agent/commit/695d66173b40b3ee9f87c3b950b54bdeff8f02d2))
* update anoncreds demo after the new getJson methods ([#584](https://github.com/hyperledger-identus/cloud-agent/issues/584)) ([d8258ee](https://github.com/hyperledger-identus/cloud-agent/commit/d8258ee5d29b94ac863f9dbf5c8eaadd66fd636e))

### Bug Fixes

* **prism-agent:** fix OAS on empty DID resolution representation ([#616](https://github.com/hyperledger-identus/cloud-agent/issues/616)) ([216ff3a](https://github.com/hyperledger-identus/cloud-agent/commit/216ff3a2ef75d824d0a6285218be01636a595a82))

### Performance Improvements

* support for credential and present-proof flows with thid ([#609](https://github.com/hyperledger-identus/cloud-agent/issues/609)) ([9cef8c0](https://github.com/hyperledger-identus/cloud-agent/commit/9cef8c03cf0a3e5601ec36b1f008dea2a738a415))

## [1.9.2](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.9.1...prism-agent-v1.9.2) (2023-07-27)

### Bug Fixes

* consumer variable nesting correction ([#606](https://github.com/hyperledger-identus/cloud-agent/issues/606)) ([40a0578](https://github.com/hyperledger-identus/cloud-agent/commit/40a0578274d33873c5189d01715244b2b34c0fea))

## [1.9.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.9.0...prism-agent-v1.9.1) (2023-07-25)

### Bug Fixes

* include helm Chart.yaml in git commit for release process ([#604](https://github.com/hyperledger-identus/cloud-agent/issues/604)) ([d0372f1](https://github.com/hyperledger-identus/cloud-agent/commit/d0372f19e74ade5627a41038b07010321d5ef600))

## [1.9.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.8.0...prism-agent-v1.9.0) (2023-07-25)

### Features

* add helm-chart for agent  ([#603](https://github.com/hyperledger-identus/cloud-agent/issues/603)) ([63f38d4](https://github.com/hyperledger-identus/cloud-agent/commit/63f38d47f4645bf6172320da5c3413c748c03729))

## [1.8.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.7.0...prism-agent-v1.8.0) (2023-07-20)

### Features

* ATL-4888 Anoncred schema type ([#590](https://github.com/hyperledger-identus/cloud-agent/issues/590)) ([a57deef](https://github.com/hyperledger-identus/cloud-agent/commit/a57deef485fea5181e8617a30ab70ca26c409b42))

### Bug Fixes

* **castor:** align DID document translation logic ([#595](https://github.com/hyperledger-identus/cloud-agent/issues/595)) ([bb1f112](https://github.com/hyperledger-identus/cloud-agent/commit/bb1f1121975c3bc8288b1d4577efd3922e5adce7))
* **prism-agent:** add did-method path segment in HD key derivation ([#596](https://github.com/hyperledger-identus/cloud-agent/issues/596)) ([a1e457a](https://github.com/hyperledger-identus/cloud-agent/commit/a1e457a8d6337e8c941b58c802f9516fe6718396))

## [1.7.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.6.0...prism-agent-v1.7.0) (2023-07-10)

### Features

* **prism-agent:** add http metrics ([#585](https://github.com/hyperledger-identus/cloud-agent/issues/585)) ([f62d7f5](https://github.com/hyperledger-identus/cloud-agent/commit/f62d7f5459f12f93224b0eb9b05caf605f54be2c))
* **prism-agent:** align DID document service handling with the spec ([#582](https://github.com/hyperledger-identus/cloud-agent/issues/582)) ([c9e69f6](https://github.com/hyperledger-identus/cloud-agent/commit/c9e69f602ef5e78848ad6d652f0ba7d4d4d2db2d))
* **prism-agent:** expose connect/issue/presentation records 'thid' and add it to REST API queries ([#583](https://github.com/hyperledger-identus/cloud-agent/issues/583)) ([9a97c7a](https://github.com/hyperledger-identus/cloud-agent/commit/9a97c7a6e5815fd80eba0f98042a49356fc1f61c))
* **prism-agent:** simple event mechanism using webhook ([#575](https://github.com/hyperledger-identus/cloud-agent/issues/575)) ([42cf8c9](https://github.com/hyperledger-identus/cloud-agent/commit/42cf8c9b47b2ac2d17e6d00b0901806e0f0e2e1d)), closes [#1](https://github.com/hyperledger-identus/cloud-agent/issues/1)

### Bug Fixes

* Adding Apollo ADR ([#573](https://github.com/hyperledger-identus/cloud-agent/issues/573)) ([e036bc8](https://github.com/hyperledger-identus/cloud-agent/commit/e036bc84446c8b7eb008f536def2adaca08e071f))
* **castor:** fix DID parser that failing to parse some DIDs ([#581](https://github.com/hyperledger-identus/cloud-agent/issues/581)) ([24b2300](https://github.com/hyperledger-identus/cloud-agent/commit/24b230023ad2812dc13aa7229163ead5eb56183d))
* **pollux:** add pagination at db level for getCredentialRecords ([#586](https://github.com/hyperledger-identus/cloud-agent/issues/586)) ([c0db5c8](https://github.com/hyperledger-identus/cloud-agent/commit/c0db5c8a2a4fee7568fb5aa43f81a2faba6936a2))

### Performance Improvements

* add k6 connection flow running in CI ([#572](https://github.com/hyperledger-identus/cloud-agent/issues/572)) ([601f934](https://github.com/hyperledger-identus/cloud-agent/commit/601f934062537c8080657b6268299f18d8201ec2))

## [1.6.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.5.1...prism-agent-v1.6.0) (2023-06-28)

### Features

* new Anoncreds Demo ([#562](https://github.com/hyperledger-identus/cloud-agent/issues/562)) ([a9a8290](https://github.com/hyperledger-identus/cloud-agent/commit/a9a8290c73fb3044c2091311c199d1e532af03f0))

### Bug Fixes

* **prism-agent:** decouple secret storage backend from agent ([#570](https://github.com/hyperledger-identus/cloud-agent/issues/570)) ([6a5f9ce](https://github.com/hyperledger-identus/cloud-agent/commit/6a5f9cef337848dadd8a54b54948db9e7edfe8ad))
* **prism-agent:** fix concurrent requests breaking DID index counter ([#571](https://github.com/hyperledger-identus/cloud-agent/issues/571)) ([e8411dd](https://github.com/hyperledger-identus/cloud-agent/commit/e8411ddb588e9dc81f2437cfbdfdcd1be42f99d1))
* **prism-agent:** use correct pairwise DIDs in presentation flow ([#568](https://github.com/hyperledger-identus/cloud-agent/issues/568)) ([ede234b](https://github.com/hyperledger-identus/cloud-agent/commit/ede234bbdcb64cb48da182b374288b549b8cf8aa))

## [1.5.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.5.0...prism-agent-v1.5.1) (2023-06-22)

### Bug Fixes

* log seed error before effect fail ([#557](https://github.com/hyperledger-identus/cloud-agent/issues/557)) ([c3a5d8e](https://github.com/hyperledger-identus/cloud-agent/commit/c3a5d8eb9e62675053f9b7fc80ee18d7a62f857c))

## [1.5.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.4.0...prism-agent-v1.5.0) (2023-06-16)

### Features

* **prism-agent:** integrate credential schema into VC issue flow ([#541](https://github.com/hyperledger-identus/cloud-agent/issues/541)) ([ab88736](https://github.com/hyperledger-identus/cloud-agent/commit/ab88736dc9d6dfec3d39f6a58619eb115f520bf8))
* **prism-agent:** integrate DID secret storage with Vault ([#543](https://github.com/hyperledger-identus/cloud-agent/issues/543)) ([ee43feb](https://github.com/hyperledger-identus/cloud-agent/commit/ee43febacddb06210065c3f812beb8c948d5c369))

## [1.4.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.3.0...prism-agent-v1.4.0) (2023-06-01)

### Features

* **prism-agent:** add support for hierarchical deterministic key with seed ([#534](https://github.com/hyperledger-identus/cloud-agent/issues/534)) ([6129baf](https://github.com/hyperledger-identus/cloud-agent/commit/6129baf1210b68decc4f264bd4a64b4009719956))

### Bug Fixes

* **prism-agent:** infinite loop in proof presentation execution ([#540](https://github.com/hyperledger-identus/cloud-agent/issues/540)) ([6a26bb7](https://github.com/hyperledger-identus/cloud-agent/commit/6a26bb78d256bdcd09918cb1e8ee5bfd5cf0dacc))

## [1.3.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.2.0...prism-agent-v1.3.0) (2023-05-23)

### Features

* restore JVM metrics endpoint capability ([#527](https://github.com/hyperledger-identus/cloud-agent/issues/527)) ([7d603f0](https://github.com/hyperledger-identus/cloud-agent/commit/7d603f09abd6042368ada6afa3685332342d6860))

## [1.2.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.1.0...prism-agent-v1.2.0) (2023-05-17)

### Features

* migrate issue endpoint to tapir ([#516](https://github.com/hyperledger-identus/cloud-agent/issues/516)) ([9b1558f](https://github.com/hyperledger-identus/cloud-agent/commit/9b1558f50003ba1c79ec2cdd9888f2e99f0534d8))
* **prism-agent:** fix infinite reprocessing of records in error ([#528](https://github.com/hyperledger-identus/cloud-agent/issues/528)) ([904a2dc](https://github.com/hyperledger-identus/cloud-agent/commit/904a2dcb09d2e907e284479c652c5f389fd0dec9))
* **prism-agent:** migrate present-proof endpoints to Tapir ([#525](https://github.com/hyperledger-identus/cloud-agent/issues/525)) ([cb01657](https://github.com/hyperledger-identus/cloud-agent/commit/cb016570b6d0a1b0de98928d6daa1cbf055d26b4))

### Bug Fixes

* **prism-agent:** refactor crypto abstraction in the walletAPI ([#522](https://github.com/hyperledger-identus/cloud-agent/issues/522)) ([e36c634](https://github.com/hyperledger-identus/cloud-agent/commit/e36c63424ed2e28fc360c6a6a5d557938d4ec01a))

## [1.1.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v1.0.0...prism-agent-v1.1.0) (2023-05-05)

### Features

* add multi-arch amd64 and arm64 support for agent docker ([#512](https://github.com/hyperledger-identus/cloud-agent/issues/512)) ([dc2608c](https://github.com/hyperledger-identus/cloud-agent/commit/dc2608c12e062a6af5d3fcf1077956281a2f0828))
* expose API for default did:peer of prism agent ([#509](https://github.com/hyperledger-identus/cloud-agent/issues/509)) ([b128292](https://github.com/hyperledger-identus/cloud-agent/commit/b128292031c547938614b21fb4cd0a377310c19e))
* **prism-agent:** migrate DID endpoint to tapir ([#511](https://github.com/hyperledger-identus/cloud-agent/issues/511)) ([9d587ff](https://github.com/hyperledger-identus/cloud-agent/commit/9d587ffc6e44da9dacb0af76e922030828831805))
* **prism-agent:** migrate did-registrar endpoint to tapir ([#517](https://github.com/hyperledger-identus/cloud-agent/issues/517)) ([88eeefd](https://github.com/hyperledger-identus/cloud-agent/commit/88eeefdad81e05197ea0b6c2bf449c4c2960e023))

### Bug Fixes

* **pollux:** ATL-3971 Fix all Deprecation Warning ([#506](https://github.com/hyperledger-identus/cloud-agent/issues/506)) ([e5225b7](https://github.com/hyperledger-identus/cloud-agent/commit/e5225b7101bf3572a85a6f0cf8ed05e93410f551))

## [1.0.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.60.2...prism-agent-v1.0.0) (2023-04-10)

### ⚠ BREAKING CHANGES

* incrementing major version for the first stable release

### Features

* add prism-agent-* prefix to the tag ([#505](https://github.com/hyperledger-identus/cloud-agent/issues/505)) ([6087f2d](https://github.com/hyperledger-identus/cloud-agent/commit/6087f2dcc77179a4bb4702e60a9669c6329ba55c))
* **castor:** add support for context in DID document & operation ([#489](https://github.com/hyperledger-identus/cloud-agent/issues/489)) ([8384fe3](https://github.com/hyperledger-identus/cloud-agent/commit/8384fe39be38f24e6b821851781c6b465e8e4bfa))
* **prism-agent:** migrate connect endpoints to Tapir ([#493](https://github.com/hyperledger-identus/cloud-agent/issues/493)) ([876dd9e](https://github.com/hyperledger-identus/cloud-agent/commit/876dd9ed4b89f7c2cf779d47bb89b8a0358743db))
* **prism-node:** add context to protobuf definition ([#487](https://github.com/hyperledger-identus/cloud-agent/issues/487)) ([e426a82](https://github.com/hyperledger-identus/cloud-agent/commit/e426a82b4f593204f1dc69c2b65c7362e8707ec6))
* Reply to Trust Pings ([#496](https://github.com/hyperledger-identus/cloud-agent/issues/496)) ([b07da78](https://github.com/hyperledger-identus/cloud-agent/commit/b07da78d3ee927c7ddfbd311d442a687b0b901a4))

## [0.60.2](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.60.1...prism-agent-v0.60.2) (2023-03-29)

### Bug Fixes

* **prism-agent:** Alight the error responses according to RFC7807. ATL-3962 ([#480](https://github.com/hyperledger-identus/cloud-agent/issues/480)) ([64b0a2a](https://github.com/hyperledger-identus/cloud-agent/commit/64b0a2a04599c30adaf64e8411e1ec95305846cd))

## [0.60.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.60.0...prism-agent-v0.60.1) (2023-03-28)

### Features

* add general test postgresql container support ([#481](https://github.com/hyperledger-identus/cloud-agent/issues/481)) ([3b1fc8d](https://github.com/hyperledger-identus/cloud-agent/commit/3b1fc8dda752bb81f5eeef31f24663ea58e0bf73))

### Bug Fixes

* update pollux to use shared postgres container ([#485](https://github.com/hyperledger-identus/cloud-agent/issues/485)) ([1dbaa15](https://github.com/hyperledger-identus/cloud-agent/commit/1dbaa1515d9a5e9008b85d1f27a9d899a8cf7425))
* update shared module for castor ([#484](https://github.com/hyperledger-identus/cloud-agent/issues/484)) ([2202a22](https://github.com/hyperledger-identus/cloud-agent/commit/2202a220fdcd404bb3fe280b3b76923fbf499b8c))
* use postgres container from shared module ([#482](https://github.com/hyperledger-identus/cloud-agent/issues/482)) ([efe4557](https://github.com/hyperledger-identus/cloud-agent/commit/efe45579a1a7a690b41aa15b30c386bd7d030137))
* use shared postgres container for tests ([#486](https://github.com/hyperledger-identus/cloud-agent/issues/486)) ([1d6aada](https://github.com/hyperledger-identus/cloud-agent/commit/1d6aada72fedf6420133451214ca27965cff245f))

## [0.60.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.59.1...prism-agent-v0.60.0) (2023-03-28)

### Features

* **prism-agent:** insert bouncy-castle security as 2nd provider globally in agent entry point ([#477](https://github.com/hyperledger-identus/cloud-agent/issues/477)) ([44f06cc](https://github.com/hyperledger-identus/cloud-agent/commit/44f06cc191d10e8f590ba56ced75c78e73089b7b))

## [0.59.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.59.0...prism-agent-v0.59.1) (2023-03-23)

### Bug Fixes

* **castor:** fix unintended ordering behavior when parsing long-form DID ([#471](https://github.com/hyperledger-identus/cloud-agent/issues/471)) ([5ed0978](https://github.com/hyperledger-identus/cloud-agent/commit/5ed097850126e6c40f7d7119c947446c813edde7))
* **pollux:** upgrade castor version ([#472](https://github.com/hyperledger-identus/cloud-agent/issues/472)) ([f269b7e](https://github.com/hyperledger-identus/cloud-agent/commit/f269b7e21623d8e60bc7069b001addc29a09f7ac))
* **prism-agent:** fix incorrect long-form parsing behavior on resolution endpoint ([#475](https://github.com/hyperledger-identus/cloud-agent/issues/475)) ([af356d6](https://github.com/hyperledger-identus/cloud-agent/commit/af356d66763197f118aee8f7d1ac7d82be05a46e))

## [0.59.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.58.0...prism-agent-v0.59.0) (2023-03-22)

### Features

* **prism-agent:** add universal-resolver compatible endpoint ([#455](https://github.com/hyperledger-identus/cloud-agent/issues/455)) ([1cbb729](https://github.com/hyperledger-identus/cloud-agent/commit/1cbb729c05da51d98163a9c99d035f2814cc9fb1))

## [0.58.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.57.0...prism-agent-v0.58.0) (2023-03-22)

### Features

* **connect:** Updated the mercury version for jwt ([#466](https://github.com/hyperledger-identus/cloud-agent/issues/466)) ([8444057](https://github.com/hyperledger-identus/cloud-agent/commit/84440573bebbdf46f901e1a8bae7ba3d84239c2d))
* **mercury:** Added the resolver and update the nimbus libray to pre… ([#465](https://github.com/hyperledger-identus/cloud-agent/issues/465)) ([67e3622](https://github.com/hyperledger-identus/cloud-agent/commit/67e362212858b5a69e3c7cb0419449cc721f857f))
* **prism-agent:** Update the connect pollux and mercury to fix to use nimbus-jose-jwt 10.0.0-preview ([#467](https://github.com/hyperledger-identus/cloud-agent/issues/467)) ([948f2fc](https://github.com/hyperledger-identus/cloud-agent/commit/948f2fc26a7b965c47e7907cf4d4eb14bb9e640f))

### Bug Fixes

* **pollux:** ATL-3900 Force use of Bouncycastle ([#463](https://github.com/hyperledger-identus/cloud-agent/issues/463)) ([5b4aa5d](https://github.com/hyperledger-identus/cloud-agent/commit/5b4aa5d2cd5afeded9efa2d43d924db8e9516670))

## [0.57.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.56.0...prism-agent-v0.57.0) (2023-03-21)

### Features

* **pollux:** Update the nimbus-jose-jwt library with a patch 10.0.0-preview ([#457](https://github.com/hyperledger-identus/cloud-agent/issues/457)) ([8c0070f](https://github.com/hyperledger-identus/cloud-agent/commit/8c0070f1789e8f42c234eadeffba08e5f0b6e836))
* **prism-agent:** Update pollux lib to 0.43.0 includes the nimbus-jo… ([#460](https://github.com/hyperledger-identus/cloud-agent/issues/460)) ([adb7000](https://github.com/hyperledger-identus/cloud-agent/commit/adb7000348a29a83ca59f0d945b1327795dd3b42))

### Bug Fixes

* update mercury to 0.21.0 ([8d42fb1](https://github.com/hyperledger-identus/cloud-agent/commit/8d42fb18206c9e599a4ab77f3c4a5162da03ba35))

## [0.56.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.55.0...prism-agent-v0.56.0) (2023-03-17)

### Features

* **prism-agent:** allow published and unpublished DIDs in the issuingDID field ([#454](https://github.com/hyperledger-identus/cloud-agent/issues/454)) ([ec107ad](https://github.com/hyperledger-identus/cloud-agent/commit/ec107ad42a66da3703ce60b1fe7697217683c89c))

## [0.55.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.54.0...prism-agent-v0.55.0) (2023-03-16)

### Features

* **pollux:** update credential schema logic ([#450](https://github.com/hyperledger-identus/cloud-agent/issues/450)) ATL-3164 ([6e22bfc](https://github.com/hyperledger-identus/cloud-agent/commit/6e22bfcd582b4e33611a91d9ce1339a222778d25)), closes [#2](https://github.com/hyperledger-identus/cloud-agent/issues/2)
* **prism-agent:** VerificationOptions are configurable for PrismAgent ([#449](https://github.com/hyperledger-identus/cloud-agent/issues/449)) ([ee93880](https://github.com/hyperledger-identus/cloud-agent/commit/ee93880dff6bd8a1fa098c3e1afd29939a6216dd))

## [0.54.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.53.0...prism-agent-v0.54.0) (2023-03-16)

### Features

* **pollux:** Add ignoreWithZeroRetries filter to SQL queries ([#445](https://github.com/hyperledger-identus/cloud-agent/issues/445)) ([d477c68](https://github.com/hyperledger-identus/cloud-agent/commit/d477c683944a30125a1caa6b5f66caa156e7f70a))
* **prism-agent:** update pollux to 0.41.0; ([#447](https://github.com/hyperledger-identus/cloud-agent/issues/447)) ([f52c5e0](https://github.com/hyperledger-identus/cloud-agent/commit/f52c5e007821e9df1b56e882d443bc31755c1ee6))

## [0.53.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.52.0...prism-agent-v0.53.0) (2023-03-14)

### Features

* **prism-agent:** Add OAS specification to the schema registry endpoint. ATL-3164 ([#438](https://github.com/hyperledger-identus/cloud-agent/issues/438)) ([91902ce](https://github.com/hyperledger-identus/cloud-agent/commit/91902ce3840227ed4d683b55060d9603320ff9fd))

## [0.52.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.51.0...prism-agent-v0.52.0) (2023-03-13)

### Features

* **connect:** Use TIMESTAMP instead of BIGINT ATL-3786 ([#431](https://github.com/hyperledger-identus/cloud-agent/issues/431)) ([fb7f2d2](https://github.com/hyperledger-identus/cloud-agent/commit/fb7f2d28a5a8eabb2f52beebd8c794a90793c6fc))
* **prism-agent:** Add filter by thid to OpenAPI when fetching records ([#436](https://github.com/hyperledger-identus/cloud-agent/issues/436)) ([af01359](https://github.com/hyperledger-identus/cloud-agent/commit/af0135904d4961d12e109004e7b001b154b1493e))

## [0.51.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.50.0...prism-agent-v0.51.0) (2023-03-10)

### Features

* **pollux:** move subjectId field from issuer side to holder side ([#434](https://github.com/hyperledger-identus/cloud-agent/issues/434)) ([434f1fd](https://github.com/hyperledger-identus/cloud-agent/commit/434f1fd7616c5f538819b1a68f4bc5ed4eeb4b5d))
* **prism-agent:** move subjectId field from issuer to holder ([#435](https://github.com/hyperledger-identus/cloud-agent/issues/435)) ([d7813ba](https://github.com/hyperledger-identus/cloud-agent/commit/d7813ba9b3d5a1c422d3d7e7fb57af4dabcb0d54))

## [0.50.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.49.0...prism-agent-v0.50.0) (2023-03-10)

### Features

* **prism-agent:** Added connectionId to the request Presentation and deleted the readme files as they are moved here <https://github.com/input-output-hk/atala-prism-building-blocks/tree/main/docs/docusaurus> ([#432](https://github.com/hyperledger-identus/cloud-agent/issues/432)) ([301022a](https://github.com/hyperledger-identus/cloud-agent/commit/301022a372e40b126b389548ef0c9acb4f0382f0))

## [0.49.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.48.4...prism-agent-v0.49.0) (2023-03-09)

### Features

* **prism-agent:** CredentialSchema DAL, model, service and repositor… ([#425](https://github.com/hyperledger-identus/cloud-agent/issues/425)) ([32f9e83](https://github.com/hyperledger-identus/cloud-agent/commit/32f9e832a7789a971a4506a8971b6ba8b06daabe))

## [0.48.4](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.48.3...prism-agent-v0.48.4) (2023-03-09)

### Features

* **mercury:** Code cleanup removed the Presentation Attachment and added to the Pollux ([#365](https://github.com/hyperledger-identus/cloud-agent/issues/365)) ([8f52b12](https://github.com/hyperledger-identus/cloud-agent/commit/8f52b1214e7bee3b18693f25a657630d85eee26f))
* **pollux:** Add method in PresentationService need for ATL-3624 ([#422](https://github.com/hyperledger-identus/cloud-agent/issues/422)) ([9a62057](https://github.com/hyperledger-identus/cloud-agent/commit/9a620578a444bd4bebb851c2ef9c62e6293ce5c1))
* **pollux:** Add method interface in PresentationService ([#427](https://github.com/hyperledger-identus/cloud-agent/issues/427)) ([bbbc797](https://github.com/hyperledger-identus/cloud-agent/commit/bbbc79709678a67210788b658c87b2bbb8599630))
* **pollux:** CredentialSchema DAL, model, service and repository [#2](https://github.com/hyperledger-identus/cloud-agent/issues/2) ([#424](https://github.com/hyperledger-identus/cloud-agent/issues/424)) ([79352f0](https://github.com/hyperledger-identus/cloud-agent/commit/79352f02cdceed6d509848e97670e1d9cfaa3632))
* **pollux:** CredentialSchema service, repository and sql ([#416](https://github.com/hyperledger-identus/cloud-agent/issues/416)) ([ffa5f7e](https://github.com/hyperledger-identus/cloud-agent/commit/ffa5f7e0e4abc4d2193a4224fbe6c99d03fa7ea8))

### Bug Fixes

* add more heap size for unit tests ([#421](https://github.com/hyperledger-identus/cloud-agent/issues/421)) ([1734533](https://github.com/hyperledger-identus/cloud-agent/commit/1734533534f911090fff45adc79b15ff61ca4122))
* **pollux:** fix 56f7aab7d3f58de51691271e1708edecc67b51b0 by exclude scala-java8-compat ([#423](https://github.com/hyperledger-identus/cloud-agent/issues/423)) ([bc7e783](https://github.com/hyperledger-identus/cloud-agent/commit/bc7e783c3e1499b6ab469178a2e524980d18d236))
* **prism-agent:** Fix for ATL-3624 ([#430](https://github.com/hyperledger-identus/cloud-agent/issues/430)) ([02fe4d8](https://github.com/hyperledger-identus/cloud-agent/commit/02fe4d8cab14eb2b54d13dc726573d07bf77b76a))

## [0.48.3](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.48.2...prism-agent-v0.48.3) (2023-03-03)

### Bug Fixes

* **pollux:** Fix Instant encoder in JdbcPresentationRepository ([#418](https://github.com/hyperledger-identus/cloud-agent/issues/418)) ([2ece2c6](https://github.com/hyperledger-identus/cloud-agent/commit/2ece2c6881430c180fa9c1bad6cdd6085be2c453))
* **prism-agent:** update pollux to 0.35.2 ([#419](https://github.com/hyperledger-identus/cloud-agent/issues/419)) ([63cd430](https://github.com/hyperledger-identus/cloud-agent/commit/63cd4305cfe10b6be5d57d1d2988536eefde35f0))

## [0.48.2](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.48.1...prism-agent-v0.48.2) (2023-03-03)

### Bug Fixes

* **prism-agent:** avoid race condition when update or deactivate DID ([#415](https://github.com/hyperledger-identus/cloud-agent/issues/415)) ([bf03674](https://github.com/hyperledger-identus/cloud-agent/commit/bf03674769f0b6163de13f4002198902fdd413e9))

## [0.48.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.48.0...prism-agent-v0.48.1) (2023-03-02)

### Bug Fixes

* **pollux:** Fix column meta_next_retry ([#413](https://github.com/hyperledger-identus/cloud-agent/issues/413)) ([c02aba8](https://github.com/hyperledger-identus/cloud-agent/commit/c02aba87adbdbea4c30ac4c1c7f7e3a8fe0aa7c4))
* **prism-agent:** update pollux to 0.35.1 ([#414](https://github.com/hyperledger-identus/cloud-agent/issues/414)) ([20770c8](https://github.com/hyperledger-identus/cloud-agent/commit/20770c84a67ca4105e964e97de7aeddbbcab5941))

## [0.48.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.47.1...prism-agent-v0.48.0) (2023-03-02)

### Features

* **prism-agent:** Added new state PresentationVerificationFailed ([#412](https://github.com/hyperledger-identus/cloud-agent/issues/412)) ([55569ed](https://github.com/hyperledger-identus/cloud-agent/commit/55569edbae03a034b84548878fe3ab19252b3bb3))

## [0.47.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.47.0...prism-agent-v0.47.1) (2023-03-02)

### Bug Fixes

* **prism-agent:** add consistency to documentation of OAS on DID endpoints ([#408](https://github.com/hyperledger-identus/cloud-agent/issues/408)) ([dd04c3f](https://github.com/hyperledger-identus/cloud-agent/commit/dd04c3fd14c76c02a7cfbb26ca52141590c48371))

## [0.47.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.46.0...prism-agent-v0.47.0) (2023-03-02)

### Features

* **prism-agent:** update pollux from 0.33.0 to 0.35.0 ([#410](https://github.com/hyperledger-identus/cloud-agent/issues/410)) ([59afe8c](https://github.com/hyperledger-identus/cloud-agent/commit/59afe8cd2acea17fd201378066b89f0bfcb8e98a))

## [0.46.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.45.1...prism-agent-v0.46.0) (2023-03-02)

### Features

* Allow override of network name ([#295](https://github.com/hyperledger-identus/cloud-agent/issues/295)) ([4ee0943](https://github.com/hyperledger-identus/cloud-agent/commit/4ee094382851e0a0745af80f0a91eadb955b9804))
* **pollux:** Add meta fields to presentation table (for retries) ([#405](https://github.com/hyperledger-identus/cloud-agent/issues/405)) ([37dee86](https://github.com/hyperledger-identus/cloud-agent/commit/37dee8616349e2d5ae858475dd4951f064bbe9db))
* **pollux:** Add retries field for ATL-3205 ([#380](https://github.com/hyperledger-identus/cloud-agent/issues/380)) ([c7efde6](https://github.com/hyperledger-identus/cloud-agent/commit/c7efde651c7b3e0260568290ab9dd58e2d955a70))
* **pollux:** Added new state PresentationVerificationFailed ([#400](https://github.com/hyperledger-identus/cloud-agent/issues/400)) ([94031f8](https://github.com/hyperledger-identus/cloud-agent/commit/94031f880f00f61be2da2dd91f22fb92246b1609))
* **prism-agent:** update libs: pollux 0.33.0; connect 0.11.0; mercury 0.20.0 ([#403](https://github.com/hyperledger-identus/cloud-agent/issues/403)) ([d724a02](https://github.com/hyperledger-identus/cloud-agent/commit/d724a02a7b5551bb8a49ddd702a6b14c42f53a81))

## [0.45.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.45.0...prism-agent-v0.45.1) (2023-02-28)

### Bug Fixes

* **prism-agent:** add uri normalization on UpdateService patch ([#401](https://github.com/hyperledger-identus/cloud-agent/issues/401)) ([6a98f70](https://github.com/hyperledger-identus/cloud-agent/commit/6a98f7015069dba781d03584ae97a40681c5a5a9))

## [0.45.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.44.0...prism-agent-v0.45.0) (2023-02-27)

### Features

* **connect:** Updated version for mercury ([#398](https://github.com/hyperledger-identus/cloud-agent/issues/398)) ([bff8d21](https://github.com/hyperledger-identus/cloud-agent/commit/bff8d21ca218c6fc8237f88d9c1288ade7b03c88))
* **pollux:** Replace UUID with String - ATL-3555 ([#397](https://github.com/hyperledger-identus/cloud-agent/issues/397)) ([cc11479](https://github.com/hyperledger-identus/cloud-agent/commit/cc1147987e68cc87545854eb9da78b5217331dcc))
* **pollux:** Updated version for mercury ([#399](https://github.com/hyperledger-identus/cloud-agent/issues/399)) ([faac0a7](https://github.com/hyperledger-identus/cloud-agent/commit/faac0a718e07ab3ccfa814b875b9684855baff5c))
* **prism-agent:** add JVM metrics endpoint, add health/version endpoint ([#390](https://github.com/hyperledger-identus/cloud-agent/issues/390)) ([6d3e5a0](https://github.com/hyperledger-identus/cloud-agent/commit/6d3e5a038b75250c3813a5454f1547247b5e5d13))

## [0.44.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.43.0...prism-agent-v0.44.0) (2023-02-27)

### Features

* **mercury:** ReportProblem Protocol updated with test and updated t… ([#396](https://github.com/hyperledger-identus/cloud-agent/issues/396)) ([403f4c2](https://github.com/hyperledger-identus/cloud-agent/commit/403f4c2c51908972fef49dee5195e7f0004e46b5))
* **pollux:** Added the Issued credetinal check at the ApiEndpoint ([#387](https://github.com/hyperledger-identus/cloud-agent/issues/387)) ([35704f4](https://github.com/hyperledger-identus/cloud-agent/commit/35704f4bd364b9fcc25cf9049533716e2fcdc36e))
* **pollux:** Moving the check when accepting the credential to crea… ([#391](https://github.com/hyperledger-identus/cloud-agent/issues/391)) ([2765845](https://github.com/hyperledger-identus/cloud-agent/commit/2765845e4255e0ff5c087563d52cf6ac0d4bd3e9))
* **prism-agent:** add pagination to did-registrar list DID endpoint ([#394](https://github.com/hyperledger-identus/cloud-agent/issues/394)) ([a21e388](https://github.com/hyperledger-identus/cloud-agent/commit/a21e38880a5d49e0616e30b9dd9b92dc01980256))
* **prsim-agent:** pollux version updated ([#392](https://github.com/hyperledger-identus/cloud-agent/issues/392)) ([409b673](https://github.com/hyperledger-identus/cloud-agent/commit/409b673e954b913d7bebf31cdf0c2e8dcee3ce03))

## [0.43.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.42.0...prism-agent-v0.43.0) (2023-02-22)

### Features

* **pollux:** upgrade castor vertion to 0.8.1 ([#388](https://github.com/hyperledger-identus/cloud-agent/issues/388)) ([757b16b](https://github.com/hyperledger-identus/cloud-agent/commit/757b16b7cf7f0fdc3f72345aad000d2b6664e744))
* **prism-agent:** fix DID service URI class and improve validation error response message ([#389](https://github.com/hyperledger-identus/cloud-agent/issues/389)) ([3d08996](https://github.com/hyperledger-identus/cloud-agent/commit/3d08996b8e92c427b317c9fac50e2d8bce85cb78))

### Bug Fixes

* **castor:** use URI that is compliant to RFC3986 ([#385](https://github.com/hyperledger-identus/cloud-agent/issues/385)) ([f92fed6](https://github.com/hyperledger-identus/cloud-agent/commit/f92fed658a20832398538dec51c9ae56b3cf3975))

## [0.42.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.41.0...prism-agent-v0.42.0) (2023-02-21)

### Features

* **prism-agent:** set OAS version to 0.41.0 ([#386](https://github.com/hyperledger-identus/cloud-agent/issues/386)) ([198643e](https://github.com/hyperledger-identus/cloud-agent/commit/198643e3856c51dcd1e68047f9269cdd71238923))

## [0.41.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.40.1...prism-agent-v0.41.0) (2023-02-20)

### Features

* **prism-agent:** do not create new Prism DID when proof presentation is generated ([#378](https://github.com/hyperledger-identus/cloud-agent/issues/378)) ([1aa856f](https://github.com/hyperledger-identus/cloud-agent/commit/1aa856f4281c23f6fd2f12b8e250ed1be285d49e))

## [0.40.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.40.0...prism-agent-v0.40.1) (2023-02-17)

### Features

* **pollux:** check prover DID is the same as VC subject when creating a VP ([#377](https://github.com/hyperledger-identus/cloud-agent/issues/377)) ([253f827](https://github.com/hyperledger-identus/cloud-agent/commit/253f8271e557297a8438e5509542026aa518835f))

### Bug Fixes

* **infra:** set local PRISM_AGENT_VERSION to 0.40.0 ([#379](https://github.com/hyperledger-identus/cloud-agent/issues/379)) ([3b348ac](https://github.com/hyperledger-identus/cloud-agent/commit/3b348acb2aff1a88cda9ddf5dbd8491871ece2ef))
* **prism-agent:** Update Mercury Connect Pollux ([#382](https://github.com/hyperledger-identus/cloud-agent/issues/382)) ([b7f02ac](https://github.com/hyperledger-identus/cloud-agent/commit/b7f02ac909098159fbc0cb4187384b0ba007524a))

### Performance Improvements

* **connect:** Improve ConnectionService's API ATL-3218 ([#370](https://github.com/hyperledger-identus/cloud-agent/issues/370)) ([66f4ad7](https://github.com/hyperledger-identus/cloud-agent/commit/66f4ad7e531b51b04e9143bd5cbd21d93270d6b9))
* **connect:** Update to Mercury 0.19.0 ([#353](https://github.com/hyperledger-identus/cloud-agent/issues/353)) ([326698b](https://github.com/hyperledger-identus/cloud-agent/commit/326698b5361e1a6fb04ddda175c5040cc7eed022))
* **pollux:** Improve CredentialService's API ATL-3225 ([#372](https://github.com/hyperledger-identus/cloud-agent/issues/372)) ([ad5602d](https://github.com/hyperledger-identus/cloud-agent/commit/ad5602dffd909b0d2a2e6652043720ac58f22193))
* **pollux:** Update to Mercury 0.19.0 ([#355](https://github.com/hyperledger-identus/cloud-agent/issues/355)) ([fb60ca8](https://github.com/hyperledger-identus/cloud-agent/commit/fb60ca8dae120ee641c46e00f073bc868f95177e))

## [0.40.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.39.0...prism-agent-v0.40.0) (2023-02-16)

### Features

* **prism-agent:** ATL-3554 clean-up OAS and remove unused endpoints and definitions ([#376](https://github.com/hyperledger-identus/cloud-agent/issues/376)) ([146cd52](https://github.com/hyperledger-identus/cloud-agent/commit/146cd52062c712de30c9862c32cba73ebe213a89))

## [0.39.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.38.0...prism-agent-v0.39.0) (2023-02-15)

### Features

* **pollux:** support issuing VC to Prism DID subject by Prism DID issuer ([#371](https://github.com/hyperledger-identus/cloud-agent/issues/371)) ([5a1acde](https://github.com/hyperledger-identus/cloud-agent/commit/5a1acdecb5e25bdf39398511d6ed1219da83e62e))
* **prism-agent:** issue credential to Prism DID holder by Prism DID issuer ([#373](https://github.com/hyperledger-identus/cloud-agent/issues/373)) ([1c1a171](https://github.com/hyperledger-identus/cloud-agent/commit/1c1a171d1ba983a9c93644ded4feafe0ed6e5294))

## [0.38.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.37.0...prism-agent-v0.38.0) (2023-02-13)

### Features

* **mercury:** Support for pleaseAck (needed for ATL-3222) ([#366](https://github.com/hyperledger-identus/cloud-agent/issues/366)) ([a2d9c98](https://github.com/hyperledger-identus/cloud-agent/commit/a2d9c9855390ea77a36943e86cfdec8a3adf0b57))
* **pollux:** implement the DAL for CRUD on the verifiable policy entity. ATL-2478 ([#368](https://github.com/hyperledger-identus/cloud-agent/issues/368)) ([b290a18](https://github.com/hyperledger-identus/cloud-agent/commit/b290a18a2aef67cfded20062e111f85bad7ad248))
* **prism-agent:** integrate VerificationPolicy DAL into the agent, update OAS and implement REST API for verification policies ([#369](https://github.com/hyperledger-identus/cloud-agent/issues/369)) ([142ff55](https://github.com/hyperledger-identus/cloud-agent/commit/142ff550a278e7f28d97c539e529fea3cc92c178))

## [0.37.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.36.0...prism-agent-v0.37.0) (2023-02-09)

### Features

* **pollux:** Added New Erorr ([#363](https://github.com/hyperledger-identus/cloud-agent/issues/363)) ([5349a0e](https://github.com/hyperledger-identus/cloud-agent/commit/5349a0edf1c297a76aaf0a5e6ed6b8d97f13b16c))
* **prism-agent:** Added challenge and domain and to protect agains r… ([#364](https://github.com/hyperledger-identus/cloud-agent/issues/364)) ([4f0b261](https://github.com/hyperledger-identus/cloud-agent/commit/4f0b261b3545c8681eccd8b38c7fa028ee840c50))

## [0.36.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.35.0...prism-agent-v0.36.0) (2023-02-07)

### Features

* **connect:** bump mercury version and fix queries ([#356](https://github.com/hyperledger-identus/cloud-agent/issues/356)) ([957bdea](https://github.com/hyperledger-identus/cloud-agent/commit/957bdeae59f566446caa14dabe7cc7310478ba8e))
* **mercury:** ATL-3424 Expose HTTP status ([#351](https://github.com/hyperledger-identus/cloud-agent/issues/351)) ([d87613c](https://github.com/hyperledger-identus/cloud-agent/commit/d87613c7cb8c892964820e3346d44b3bf46114fd))
* **pollux:** bump mercury version and fix queries ([#357](https://github.com/hyperledger-identus/cloud-agent/issues/357)) ([28f779a](https://github.com/hyperledger-identus/cloud-agent/commit/28f779a5a8bb24eb5ffd8fce0b5cd0a4e8387132))
* **prism-agent:** ATL-3349 connection state issue and perf improvements ([#359](https://github.com/hyperledger-identus/cloud-agent/issues/359)) ([c77f160](https://github.com/hyperledger-identus/cloud-agent/commit/c77f160043262662ce7d5d9c6f75cd893bcab68d))

## [0.35.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.34.0...prism-agent-v0.35.0) (2023-02-07)

### Features

* **connect:** Add method reportProcessingFailure ([#345](https://github.com/hyperledger-identus/cloud-agent/issues/345)) ([ebf583c](https://github.com/hyperledger-identus/cloud-agent/commit/ebf583cff2c00d40e7d534305f21fa47d35eb087))
* **connect:** support connect records retrieval by states ([#349](https://github.com/hyperledger-identus/cloud-agent/issues/349)) ([7673278](https://github.com/hyperledger-identus/cloud-agent/commit/7673278b2a9a88ec503c44b31dd14902462e43a4))
* **pollux:** support issue/presentation records retrieval by states ([#350](https://github.com/hyperledger-identus/cloud-agent/issues/350)) ([f810ee3](https://github.com/hyperledger-identus/cloud-agent/commit/f810ee31a7e5a523efa193e9efaadc3f67b2783f))
* **prism-agent:** Upgrade libs ([#344](https://github.com/hyperledger-identus/cloud-agent/issues/344)) ([64a7857](https://github.com/hyperledger-identus/cloud-agent/commit/64a7857ecc0acc6d940e7f2fcce1c68c8163562c))

### Performance Improvements

* **connect:** Update to Mercury 0.17.0 ([#346](https://github.com/hyperledger-identus/cloud-agent/issues/346)) ([8823325](https://github.com/hyperledger-identus/cloud-agent/commit/8823325e88604c970d3dd6a94c4573f5658a8ca2))
* **pollux:** Update to Mercury 0.17.0 ([#347](https://github.com/hyperledger-identus/cloud-agent/issues/347)) ([8e90c5f](https://github.com/hyperledger-identus/cloud-agent/commit/8e90c5fe0ad25ac40c7e7df14ed952c1d68333bd))

## [0.34.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.33.0...prism-agent-v0.34.0) (2023-02-06)

### Features

* **castor:** align castor implementation with DID spec ([#336](https://github.com/hyperledger-identus/cloud-agent/issues/336)) ([7992b80](https://github.com/hyperledger-identus/cloud-agent/commit/7992b80a35df9e1308b811bfa0492e9d17975f5a))
* **connect:** Add retries field for ATL-3202 ([#335](https://github.com/hyperledger-identus/cloud-agent/issues/335)) ([7cbcebc](https://github.com/hyperledger-identus/cloud-agent/commit/7cbcebc7c9229e1dac47b3f31b3d1d6621e739d3))
* **pollux:** upgrade castor and update DID public key extraction logic ([#339](https://github.com/hyperledger-identus/cloud-agent/issues/339)) ([00a6026](https://github.com/hyperledger-identus/cloud-agent/commit/00a6026b10434bb3fa17c1d11b581316f5295237))
* **prism-agent:** upgrade castor and pollux and align OAS to the DID spec ([#342](https://github.com/hyperledger-identus/cloud-agent/issues/342)) ([b8643a8](https://github.com/hyperledger-identus/cloud-agent/commit/b8643a81710300082bfaceb08f906d364869d405))

## [0.33.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.32.0...prism-agent-v0.33.0) (2023-02-02)

### Features

* **mercury:** Expose pthid on the connection protocol ([#333](https://github.com/hyperledger-identus/cloud-agent/issues/333)) ([82eca31](https://github.com/hyperledger-identus/cloud-agent/commit/82eca31f2dd29ab1ce96ae4948208883499d23c0))
* **pollux:** Dummy commit to make a release ([#337](https://github.com/hyperledger-identus/cloud-agent/issues/337)) ([2168e13](https://github.com/hyperledger-identus/cloud-agent/commit/2168e130e84ac2b16805e4ab3544a732f41143c6))
* **pollux:** Dummy commit to make a release (2) ([#338](https://github.com/hyperledger-identus/cloud-agent/issues/338)) ([4c7516c](https://github.com/hyperledger-identus/cloud-agent/commit/4c7516c20c65906c3188f04ab1b7dc1dfd2e7822))
* **pollux:** get rid of 'drop(1)' and 'dropRight(1)' on credential attachment value ([#341](https://github.com/hyperledger-identus/cloud-agent/issues/341)) ([b0a14d4](https://github.com/hyperledger-identus/cloud-agent/commit/b0a14d459cd806353b73461194a847ec03551332))
* **pollux:** upgrade mercury to 0.16.0 ([#327](https://github.com/hyperledger-identus/cloud-agent/issues/327)) ([f4b99fc](https://github.com/hyperledger-identus/cloud-agent/commit/f4b99fc00ff61e003d13b79b6d05d70b0fcf70c9))
* **prism-agent:** Upgrade mercury connect pollux ([#340](https://github.com/hyperledger-identus/cloud-agent/issues/340)) ([3758232](https://github.com/hyperledger-identus/cloud-agent/commit/3758232448ae66fbc444c487f32917a106645473))

## [0.32.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.31.1...prism-agent-v0.32.0) (2023-01-27)

### Features

* **castor:** add support DID deactivate operation ([#325](https://github.com/hyperledger-identus/cloud-agent/issues/325)) ([5ceb7e9](https://github.com/hyperledger-identus/cloud-agent/commit/5ceb7e953664c15457e6a6657041442a5c3d761b))
* **mercury:** Split DidComm into DidAgent and DidOps ([#319](https://github.com/hyperledger-identus/cloud-agent/issues/319)) ([e12ca22](https://github.com/hyperledger-identus/cloud-agent/commit/e12ca22daaecb775894067f36f82d1ed0e3e6ea0))
* **pollux:** upgrade castor to 0.7.0 ([#330](https://github.com/hyperledger-identus/cloud-agent/issues/330)) ([de180a6](https://github.com/hyperledger-identus/cloud-agent/commit/de180a6e7b939f6139618eb57003b1036bf300e0))
* **prism-agent:** add deactivate DID endpoint ([#326](https://github.com/hyperledger-identus/cloud-agent/issues/326)) ([29a804f](https://github.com/hyperledger-identus/cloud-agent/commit/29a804fce71d2c500cec9ab8db21de84f9016c95))

## [0.31.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.31.0...prism-agent-v0.31.1) (2023-01-26)

### Bug Fixes

* **prism-agent:** remove deprecated did-auth endpoints ([#324](https://github.com/hyperledger-identus/cloud-agent/issues/324)) ([a934cd4](https://github.com/hyperledger-identus/cloud-agent/commit/a934cd4ac48f4ba4724681eeff92f4c67c009940))

## [0.31.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.30.0...prism-agent-v0.31.0) (2023-01-25)

### Features

* **prism-agent:** remove connection deletion enpoint from OAS spec ([#323](https://github.com/hyperledger-identus/cloud-agent/issues/323)) ([cb17acf](https://github.com/hyperledger-identus/cloud-agent/commit/cb17acf782500c0885c077570392423deee9a2f2))

## [0.30.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.29.0...prism-agent-v0.30.0) (2023-01-23)

### Features

* **pollux:** upgrade castor to 0.6.0 ([#320](https://github.com/hyperledger-identus/cloud-agent/issues/320)) ([c97624d](https://github.com/hyperledger-identus/cloud-agent/commit/c97624d9b5542999db91bec96fcc8dff3177cca1))
* **prism-agent:** enable update DID operation on prism-agent ([#307](https://github.com/hyperledger-identus/cloud-agent/issues/307)) ([a57365f](https://github.com/hyperledger-identus/cloud-agent/commit/a57365f8c063958e7a41e01fde71653843dafe24))

## [0.29.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.28.0...prism-agent-v0.29.0) (2023-01-20)

### Features

* **castor:** add support for DID update operation ([#306](https://github.com/hyperledger-identus/cloud-agent/issues/306)) ([27b77b2](https://github.com/hyperledger-identus/cloud-agent/commit/27b77b24680fef57d55f64e4f3448232cc73d323))
* **mercury-mediator:** Update to Mercury 0.12.0; Fix code ([#257](https://github.com/hyperledger-identus/cloud-agent/issues/257)) ([e990a92](https://github.com/hyperledger-identus/cloud-agent/commit/e990a92a377e92925486f525a7c7ea977fe2797a))
* **mercury:** Add extra method invitation2Connect ([#309](https://github.com/hyperledger-identus/cloud-agent/issues/309)) ([ea3d819](https://github.com/hyperledger-identus/cloud-agent/commit/ea3d81935cd0ccca032310021a89d0a9bc016d48))
* **mercury:** replace method buildBaseAttachment with buildBase64Attachment ([#278](https://github.com/hyperledger-identus/cloud-agent/issues/278)) ([94c7895](https://github.com/hyperledger-identus/cloud-agent/commit/94c7895a5630251aacbdcb0445e7d3da9b225d4c))
* **pollux:** add dal for the credential schema ATL-1342 ([#298](https://github.com/hyperledger-identus/cloud-agent/issues/298)) ([f43320f](https://github.com/hyperledger-identus/cloud-agent/commit/f43320fa4ad375551496f511fb68b37778c79a77))
* **pollux:** fix the lookup count in the credential schema DAL ([#315](https://github.com/hyperledger-identus/cloud-agent/issues/315)) ([e0831e8](https://github.com/hyperledger-identus/cloud-agent/commit/e0831e884d4edee131d9e3999ab0edb5144a5d04))
* **prism-agent:** implement DAL for the credential schema. ATL-1334 ([3d0c642](https://github.com/hyperledger-identus/cloud-agent/commit/3d0c6426cc7fddbce41de16a1c85f4242e046c6a))

### Bug Fixes

* **mercury:** Add compilation flags and fix code ([#302](https://github.com/hyperledger-identus/cloud-agent/issues/302)) ([43014c5](https://github.com/hyperledger-identus/cloud-agent/commit/43014c52b1671328956cd5913c2ebb4f6d206d89))

## [0.28.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.27.0...prism-agent-v0.28.0) (2023-01-06)

### Features

* **castor:** upgrade node client for new key type ([#287](https://github.com/hyperledger-identus/cloud-agent/issues/287)) ([5661e76](https://github.com/hyperledger-identus/cloud-agent/commit/5661e760c4e8c129f5947621dbf47b4ae3048bf4))
* **pollux:** added column credentionals_to_use and new state present… ([#288](https://github.com/hyperledger-identus/cloud-agent/issues/288)) ([#290](https://github.com/hyperledger-identus/cloud-agent/issues/290)) ([174fe52](https://github.com/hyperledger-identus/cloud-agent/commit/174fe52d2d64c75a88822662e1921d22abe26fa8))
* **prism-node:** align Prism DID protobuf key type ([#286](https://github.com/hyperledger-identus/cloud-agent/issues/286)) ([a533507](https://github.com/hyperledger-identus/cloud-agent/commit/a5335075be287f6d5582deba849255d2156d9542))
* **prismagent:** prism did creation and use  updated protocol state ([#291](https://github.com/hyperledger-identus/cloud-agent/issues/291)) ([cc7c533](https://github.com/hyperledger-identus/cloud-agent/commit/cc7c53380faba9afe36006b8bf68621862c9b902))
* **pullux:** dummy commit for release ([#293](https://github.com/hyperledger-identus/cloud-agent/issues/293)) ([859c29e](https://github.com/hyperledger-identus/cloud-agent/commit/859c29ea968c79154fd007e91e7cbe30116c3c95))

## [0.27.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.26.0...prism-agent-v0.27.0) (2022-12-22)

### Features

* **mercury:** Improve support for ForwardMessage in MessagingService ([#269](https://github.com/hyperledger-identus/cloud-agent/issues/269)) ([4385440](https://github.com/hyperledger-identus/cloud-agent/commit/4385440d4b202af97aad5f17f3327bad452b7930))
* **prism-agent:** implement JDBC did nonsecret storage ([#284](https://github.com/hyperledger-identus/cloud-agent/issues/284)) ([7e116a3](https://github.com/hyperledger-identus/cloud-agent/commit/7e116a38b44408b3d0c875bc79415ea6d0579ffa))

## [0.26.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.25.0...prism-agent-v0.26.0) (2022-12-20)

### Features

* **prism-agent:** add 'GET /present-proof/presentations/{id}' endpoint ([#282](https://github.com/hyperledger-identus/cloud-agent/issues/282)) ([030a257](https://github.com/hyperledger-identus/cloud-agent/commit/030a257efa89f5fc473549dc17657160fda0b26b))

## [0.25.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.24.0...prism-agent-v0.25.0) (2022-12-20)

### Features

* **pollux:** Added JWT verfication ([#280](https://github.com/hyperledger-identus/cloud-agent/issues/280)) ([a87dc17](https://github.com/hyperledger-identus/cloud-agent/commit/a87dc1718734fb4022afaaacaea13853bfff7ff8))
* **prism-agnet:** Verifiable Credential ([#281](https://github.com/hyperledger-identus/cloud-agent/issues/281)) ([ae74e20](https://github.com/hyperledger-identus/cloud-agent/commit/ae74e20080cfcac7263c7540c8ed5b82070428e1))

## [0.24.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.23.0...prism-agent-v0.24.0) (2022-12-20)

### Features

* **prism-agent:** allow connection reuse when creating credential offer ([#276](https://github.com/hyperledger-identus/cloud-agent/issues/276)) ([eff3918](https://github.com/hyperledger-identus/cloud-agent/commit/eff3918c98ca3f2edc708688edd2ece63f7f5aa9))

## [0.23.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.22.0...prism-agent-v0.23.0) (2022-12-19)

### Features

* **connect:** bump mercury version ([#272](https://github.com/hyperledger-identus/cloud-agent/issues/272)) ([70f878c](https://github.com/hyperledger-identus/cloud-agent/commit/70f878c81bbffd73228d2a40b55295b74c918ba9))
* **mercury:** dumb release commit ([#270](https://github.com/hyperledger-identus/cloud-agent/issues/270)) ([8a1e5c3](https://github.com/hyperledger-identus/cloud-agent/commit/8a1e5c3d3e58a7e29c9c3f4692798270fd58a5ff))
* **mercury:** empty release commit ([#268](https://github.com/hyperledger-identus/cloud-agent/issues/268)) ([21b5dec](https://github.com/hyperledger-identus/cloud-agent/commit/21b5dec2589dd9dce08e89497075a0d4741f100e))
* **mercury:** forward messaging to mediator ([#264](https://github.com/hyperledger-identus/cloud-agent/issues/264)) ([1170e2f](https://github.com/hyperledger-identus/cloud-agent/commit/1170e2f093ec2a2f70a56eda22d3721c6063ddc4))
* **mercury:** Support a mediator in service uri field ([#263](https://github.com/hyperledger-identus/cloud-agent/issues/263)) ([88ad415](https://github.com/hyperledger-identus/cloud-agent/commit/88ad415a3770abb4a5f40ff6fe5e4e1ebf1ab996))
* **pollux:** bump mercury version ([#271](https://github.com/hyperledger-identus/cloud-agent/issues/271)) ([a31f379](https://github.com/hyperledger-identus/cloud-agent/commit/a31f379321bbf9dc43eb851365e880572cb29bde))
* **prism-agent:** bump mercury, pollux and connect versions ([#273](https://github.com/hyperledger-identus/cloud-agent/issues/273)) ([ce4758b](https://github.com/hyperledger-identus/cloud-agent/commit/ce4758bf0e1a34b0953c05eea63ae06a389fc532))

## [0.22.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.21.0...prism-agent-v0.22.0) (2022-12-16)

### Features

* **prism-agent:** use pairwise DID in proof presentation flow ([#260](https://github.com/hyperledger-identus/cloud-agent/issues/260)) ([bb04ca4](https://github.com/hyperledger-identus/cloud-agent/commit/bb04ca4c4d2a2c18e8be7f2932c0546888ffce05))

## [0.21.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.20.0...prism-agent-v0.21.0) (2022-12-15)

### Features

* **pollux:** [ATL-2706] Universal Verification Method ([#258](https://github.com/hyperledger-identus/cloud-agent/issues/258)) ([d0c36f4](https://github.com/hyperledger-identus/cloud-agent/commit/d0c36f44a2b5dff427aef54d745b3b4e19f3f766))
* **prism-agent:** integrate latest pollux version ([#259](https://github.com/hyperledger-identus/cloud-agent/issues/259)) ([d199f0d](https://github.com/hyperledger-identus/cloud-agent/commit/d199f0db0c40683bfafdeec4726b517172f8a769))

## [0.20.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.19.0...prism-agent-v0.20.0) (2022-12-14)

### Features

* **pollux:** integrate castor resolver to pollux-jwt-vc library ([#250](https://github.com/hyperledger-identus/cloud-agent/issues/250)) ([ea10db5](https://github.com/hyperledger-identus/cloud-agent/commit/ea10db5f693758f8cbff5be839d691806f161158))
* **prism-agent:** fix DB connection pool duplication issue by providing ZLayer globally ([#256](https://github.com/hyperledger-identus/cloud-agent/issues/256)) ([4424de1](https://github.com/hyperledger-identus/cloud-agent/commit/4424de14917988cc6f8e406624bf7cf20db27455))

## [0.19.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.18.0...prism-agent-v0.19.0) (2022-12-14)

### Features

* **pollux:** use dedicated pairwise PeerDIDs instead of a global one ([#252](https://github.com/hyperledger-identus/cloud-agent/issues/252)) ([38a9b95](https://github.com/hyperledger-identus/cloud-agent/commit/38a9b95a254e53483c15d7ee381f82a1d7556a18))
* **prism-agent:** Use pairwise PeerDIDs in connect and issue ([#253](https://github.com/hyperledger-identus/cloud-agent/issues/253)) ([01519ff](https://github.com/hyperledger-identus/cloud-agent/commit/01519ffc7d608c3d17ffa06242bf7324681c31d1))

## [0.18.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.17.1...prism-agent-v0.18.0) (2022-12-14)

### Features

* **connect:** updated  the version for mercury ([#246](https://github.com/hyperledger-identus/cloud-agent/issues/246)) ([7629eab](https://github.com/hyperledger-identus/cloud-agent/commit/7629eab985354a64d4a7c7f5814ae4a84a48ab31))
* **mercury:** New Messaging Service with support for forward messaging ([#244](https://github.com/hyperledger-identus/cloud-agent/issues/244)) ([7f511e0](https://github.com/hyperledger-identus/cloud-agent/commit/7f511e00980c24aa0ce8be25b8dedf9ecf524790))
* **pollux:** [ATL-2733] Allow for dates in seconds for exp, iss and iat ([#249](https://github.com/hyperledger-identus/cloud-agent/issues/249)) ([01af7c8](https://github.com/hyperledger-identus/cloud-agent/commit/01af7c8bde32d95743957e564868d1f250f6ed3e))
* **pollux:** updated  the version for mercury ([#245](https://github.com/hyperledger-identus/cloud-agent/issues/245)) ([a2af672](https://github.com/hyperledger-identus/cloud-agent/commit/a2af6726264613247b008429e42c8470aa58c034))
* **prism-agent:** Update mercury to 0.12.0 & pollux to 0.10.0 & connect to 0.4.0 ([#248](https://github.com/hyperledger-identus/cloud-agent/issues/248)) ([33ed7ba](https://github.com/hyperledger-identus/cloud-agent/commit/33ed7ba56e95bcecc75f43b8fbce77b685530588))

### Bug Fixes

* **castor:** add missing did document field ([#251](https://github.com/hyperledger-identus/cloud-agent/issues/251)) ([27370ab](https://github.com/hyperledger-identus/cloud-agent/commit/27370abae1a26e0441d81ac10619539a7bc60aaf))

## [0.17.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.17.0...prism-agent-v0.17.1) (2022-12-13)

### Bug Fixes

* **prism-agent:** switch datetime format to offsetdatetime. ATL-2723 ([#243](https://github.com/hyperledger-identus/cloud-agent/issues/243)) ([6903afa](https://github.com/hyperledger-identus/cloud-agent/commit/6903afa8d3ba226f02b1dce7665cf5adf3fc09e6))

## [0.17.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.16.1...prism-agent-v0.17.0) (2022-12-12)

### Features

* **mercury:** Make Message class and protocol-connection more robust ([#235](https://github.com/hyperledger-identus/cloud-agent/issues/235)) ([ca8a638](https://github.com/hyperledger-identus/cloud-agent/commit/ca8a638ef1640045202b713727467a428ab18a2c))
* **pollux:** [ATL-2679] Improve Error Hanlding and Verification ([#239](https://github.com/hyperledger-identus/cloud-agent/issues/239)) ([6348e13](https://github.com/hyperledger-identus/cloud-agent/commit/6348e1339b1c8f8b6b1646c8730c526eac99cf4a))

## [0.16.1](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.16.0...prism-agent-v0.16.1) (2022-12-12)

### Features

* **pollux:** Added new state ([#234](https://github.com/hyperledger-identus/cloud-agent/issues/234)) ([b704abc](https://github.com/hyperledger-identus/cloud-agent/commit/b704abca020fc0d6f03f625aead2d206dba1818c))

### Bug Fixes

* **infra:** change didcomm endpoint - remove suffix ([#240](https://github.com/hyperledger-identus/cloud-agent/issues/240)) ([2dcf5e3](https://github.com/hyperledger-identus/cloud-agent/commit/2dcf5e3277428be9c26ba998a6245ba193450713))
* **prism-agent:** didcomm endpoint now exposed in docker file and with correct path ([#241](https://github.com/hyperledger-identus/cloud-agent/issues/241)) ([405f367](https://github.com/hyperledger-identus/cloud-agent/commit/405f3672d48dd47559f10f89db5cd7051a0c9eeb))

## [0.16.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.15.0...prism-agent-v0.16.0) (2022-12-12)

### Features

* **castor:** upgrade castor with service in protobuf definition ([#224](https://github.com/hyperledger-identus/cloud-agent/issues/224)) ([8223740](https://github.com/hyperledger-identus/cloud-agent/commit/82237403a2f53942ebcc7ed14091f0de9b970553))
* **mercury:** Message field 'to' must be an Array ([#215](https://github.com/hyperledger-identus/cloud-agent/issues/215)) ([d4e2c57](https://github.com/hyperledger-identus/cloud-agent/commit/d4e2c57822c081f572fcde95fd1f2b7e2af7a946))
* **pollux:** Added a new column to the table to store the issued credential ([cd416bf](https://github.com/hyperledger-identus/cloud-agent/commit/cd416bf2ddbb0a7f24826316aa590747b33d2a38))
* **prism-agent:** upgrade castor version with DID serviceEndpoint ([#229](https://github.com/hyperledger-identus/cloud-agent/issues/229)) ([0ba3b89](https://github.com/hyperledger-identus/cloud-agent/commit/0ba3b892b25e66c081c931b99c43e60d3c51af2a))

### Bug Fixes

* **infra:** adds execution chmod to init-script ([#220](https://github.com/hyperledger-identus/cloud-agent/issues/220)) ([17038d3](https://github.com/hyperledger-identus/cloud-agent/commit/17038d3f2ebdadb284bf2ed4057db68f2a0a5742))

## [0.15.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.14.0...prism-agent-v0.15.0) (2022-12-09)

### Features

* **pollux:** [ATL-2640] JWT Presentation Signature Verification Using DidResolver ([#212](https://github.com/hyperledger-identus/cloud-agent/issues/212)) ([258c6c0](https://github.com/hyperledger-identus/cloud-agent/commit/258c6c0d6a1e2ed63947fbdaa688c928b099749d))
* **pollux:** [ATL-2643] Verify All JWT Presentation Enclosed Credentials ([#221](https://github.com/hyperledger-identus/cloud-agent/issues/221)) ([202f565](https://github.com/hyperledger-identus/cloud-agent/commit/202f56558eafbab58e3bfa1e087eb6e58b7388f1))
* **prism-agent:** bump dependencies version ([#228](https://github.com/hyperledger-identus/cloud-agent/issues/228)) ([8a6bad5](https://github.com/hyperledger-identus/cloud-agent/commit/8a6bad539334775d25ef9b5cf0eb042832ecd272))
* **prism-node:** add service to prism-node protobuf ([#222](https://github.com/hyperledger-identus/cloud-agent/issues/222)) ([680ef7b](https://github.com/hyperledger-identus/cloud-agent/commit/680ef7bbc616d55779b0543641c92363454cd92f))
* **prism-node:** move api directory / make client releasable ([#223](https://github.com/hyperledger-identus/cloud-agent/issues/223)) ([53841d6](https://github.com/hyperledger-identus/cloud-agent/commit/53841d6fd2aea24ca4e2a2d6935ef70b763f65d1))

### Bug Fixes

* **castor:** README.md is added to increase the version of the castor library ([cdd4772](https://github.com/hyperledger-identus/cloud-agent/commit/cdd47728c17f7c99d36f868e46c75cc7cf4afa92))
* **connect:** bump mercury version to 1.10.1 and touch README.md ([e13a1bd](https://github.com/hyperledger-identus/cloud-agent/commit/e13a1bdcf2eec2c6059d8e9b4f4a587ff6aa15a6))
* **mercury:** simple commit to increase the version of mercury library ([5ffb0cc](https://github.com/hyperledger-identus/cloud-agent/commit/5ffb0cc9e7ca8e63feacc6e915ab026681a30f24))
* **pollux:** upgrade mercury lib to 1.10.1 ([ee27755](https://github.com/hyperledger-identus/cloud-agent/commit/ee2775534f6207a6fed6332c938e6249d62168df))

## [0.14.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.13.0...prism-agent-v0.14.0) (2022-12-07)

### Features

* **infra:** switch to single instance of postgres for running locally ([#203](https://github.com/hyperledger-identus/cloud-agent/issues/203)) ([32e33f1](https://github.com/hyperledger-identus/cloud-agent/commit/32e33f109f834386cfda5168a00e8407ca136e2e))

## [0.13.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.12.0...prism-agent-v0.13.0) (2022-12-07)

### Features

* **pollux:** [ATL-2639] JWT Presentation Temporal Verification ([#204](https://github.com/hyperledger-identus/cloud-agent/issues/204)) ([4ff51b6](https://github.com/hyperledger-identus/cloud-agent/commit/4ff51b65a8738086f4fa6599288ff0903509ac97))
* **pollux:** remove issuanceDate to fix the main branch ([#207](https://github.com/hyperledger-identus/cloud-agent/issues/207)) ([713e659](https://github.com/hyperledger-identus/cloud-agent/commit/713e6595e9fbf12224b57c2fadd2c5c371a2ec67))
* **prism-agent:** remove unused / deprecated DID endpoints ([#213](https://github.com/hyperledger-identus/cloud-agent/issues/213)) ([0308b4f](https://github.com/hyperledger-identus/cloud-agent/commit/0308b4fdc6da78643354391234a6139d79cd8e90))

## [0.12.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.11.0...prism-agent-v0.12.0) (2022-12-06)

### Features

* **prism-agent:** implement get DIDs endpoint ([#198](https://github.com/hyperledger-identus/cloud-agent/issues/198)) ([d5e08ab](https://github.com/hyperledger-identus/cloud-agent/commit/d5e08ab16aa629805264a726c2cb9a5803226703))

## [0.11.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.10.0...prism-agent-v0.11.0) (2022-12-06)

### Features

* **mercury:** clean unwanted dependencies ([#206](https://github.com/hyperledger-identus/cloud-agent/issues/206)) ([1749e9d](https://github.com/hyperledger-identus/cloud-agent/commit/1749e9d3aa54a912a997fd4b5bcc131cc3d23bce))
* **mercury:** Update protocol-present-proof to v3 & add our extensions ([#192](https://github.com/hyperledger-identus/cloud-agent/issues/192)) ([28b083a](https://github.com/hyperledger-identus/cloud-agent/commit/28b083a73205413557b59e8756c6b33e354c3c39))
* **pollux:** [ATL-2235] Verfiable Prensentation Adjustments ([#201](https://github.com/hyperledger-identus/cloud-agent/issues/201)) ([42d23fb](https://github.com/hyperledger-identus/cloud-agent/commit/42d23fbee6a778786f8f083a193e1a0603c68717))
* **pollux:** Pollux part for the Verification Flow - ATL-2117 ([#202](https://github.com/hyperledger-identus/cloud-agent/issues/202)) ([2c967f1](https://github.com/hyperledger-identus/cloud-agent/commit/2c967f130eee455a0d596cc23e430da369b2e297))
* **prism-agent:** verification policies pagination. ATL-1334 ([#205](https://github.com/hyperledger-identus/cloud-agent/issues/205)) ([403eb38](https://github.com/hyperledger-identus/cloud-agent/commit/403eb3821daab718d97f0760ba2d71e065258665))

## [0.10.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.9.0...prism-agent-v0.10.0) (2022-12-02)

### Features

* **infra:** switch to APISIX for local running instead of HAProxy ([#196](https://github.com/hyperledger-identus/cloud-agent/issues/196)) ([e3a1aa6](https://github.com/hyperledger-identus/cloud-agent/commit/e3a1aa6c75dc89eee4c7c034c73dc1b1b2234e33))
* **pollux:** validate the current record state on each protocol action received ([#193](https://github.com/hyperledger-identus/cloud-agent/issues/193)) ([6fffde2](https://github.com/hyperledger-identus/cloud-agent/commit/6fffde28bee50b130a0f1f8b5f4dae80ec488498))
* **prism-agent:** fix prism-agent major issues ([#199](https://github.com/hyperledger-identus/cloud-agent/issues/199)) ([1dc7339](https://github.com/hyperledger-identus/cloud-agent/commit/1dc733909374ba31f4655003dafcf4479fddc70b))

## [0.9.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.8.0...prism-agent-v0.9.0) (2022-12-02)

### Features

* **prism-agent:** implement pagination with navigation for schema-registry ([#195](https://github.com/hyperledger-identus/cloud-agent/issues/195)) ([726e2d9](https://github.com/hyperledger-identus/cloud-agent/commit/726e2d95ae879867d0932d93ac51e75b177bcb6c))

## [0.8.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.7.0...prism-agent-v0.8.0) (2022-12-02)

### Features

* **castor:** implement translation of Node DidData to W3C DidDocument ([#182](https://github.com/hyperledger-identus/cloud-agent/issues/182)) ([d72159f](https://github.com/hyperledger-identus/cloud-agent/commit/d72159fdd2ae1797c33bd425443dc632b9e8ebac))
* **infra:** improved scripts for runinng locally or develping locally ([#153](https://github.com/hyperledger-identus/cloud-agent/issues/153)) ([df24ad9](https://github.com/hyperledger-identus/cloud-agent/commit/df24ad99d07661ba6d0561f9269afa5f4a3c51f6))
* **prism-agent:** implement DID resolution endpoint ([#184](https://github.com/hyperledger-identus/cloud-agent/issues/184)) ([7fba9b0](https://github.com/hyperledger-identus/cloud-agent/commit/7fba9b0bd4eac3c5b524a6db0590abcd97839afc))

## [0.7.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.6.0...prism-agent-v0.7.0) (2022-12-01)

### Features

* **mercury:** New DidValidator ([#180](https://github.com/hyperledger-identus/cloud-agent/issues/180)) ([e5d1810](https://github.com/hyperledger-identus/cloud-agent/commit/e5d1810a157db7036490a0721404c7af0b825266))
* **pollux:** alight the OAS for schema registry ([#189](https://github.com/hyperledger-identus/cloud-agent/issues/189)) ([4528c57](https://github.com/hyperledger-identus/cloud-agent/commit/4528c573fc89b9af60f6bc014e7f34433a181cb8))
* **pollux:** cleanup the code of IssueCredentialApi ([16d5fdb](https://github.com/hyperledger-identus/cloud-agent/commit/16d5fdbadf20c1597bf42b4e366f71623804dfc4))
* **pollux:** cleanup the OAS from Issue Credentials and other unused tags ([79170f8](https://github.com/hyperledger-identus/cloud-agent/commit/79170f8722053de9e477118f3f9443c97f27c512))
* **prism-agent:** upgrade castor on prism-agent ([#141](https://github.com/hyperledger-identus/cloud-agent/issues/141)) ([e85e7c0](https://github.com/hyperledger-identus/cloud-agent/commit/e85e7c09019f3e1d55f36b73fa081f42c19d4218))

## [0.6.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.5.0...prism-agent-v0.6.0) (2022-11-29)

### Features

* **prism-agent:** add connect/issue doc + allow local execution of multiple Prism Agent instances ([#178](https://github.com/hyperledger-identus/cloud-agent/issues/178)) ([dc8d86b](https://github.com/hyperledger-identus/cloud-agent/commit/dc8d86b1ba87d747c5ac0089573ddd8c2ab62f5e))

## [0.5.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.4.0...prism-agent-v0.5.0) (2022-11-28)

### Features

* **castor:** update castor to use prism-node protobuf ([#136](https://github.com/hyperledger-identus/cloud-agent/issues/136)) ([5e3445f](https://github.com/hyperledger-identus/cloud-agent/commit/5e3445ff0c93e4fdeead6f74e27c5eb8abb48dcc))
* **connect:** bump up shared version to use 0.2.0 ([56cc1c3](https://github.com/hyperledger-identus/cloud-agent/commit/56cc1c38caa313846c61253527accb456ccc476a))
* **connect:** implement Connect protocol ([#172](https://github.com/hyperledger-identus/cloud-agent/issues/172)) ([b707792](https://github.com/hyperledger-identus/cloud-agent/commit/b707792ba7c3a48f25089b5224b90dd186733539))
* **pollux:** fix shared version to 0.2.0 ([4809414](https://github.com/hyperledger-identus/cloud-agent/commit/48094148f26b79ed76a044cb65e6e4aaf00aebb1))
* **pollux:** minor changes related to connect implementation ([#171](https://github.com/hyperledger-identus/cloud-agent/issues/171)) ([4573ef8](https://github.com/hyperledger-identus/cloud-agent/commit/4573ef8122a9b0d2539f9e186a156982480a5d89))
* **pollux:** schema registry lookup and verification policies REST API ATL-1334 ([#168](https://github.com/hyperledger-identus/cloud-agent/issues/168)) ([d75b36b](https://github.com/hyperledger-identus/cloud-agent/commit/d75b36bd472c1cc2f15d775596a2675cda469754))
* **prism-agent:** implement Connect flow ([#130](https://github.com/hyperledger-identus/cloud-agent/issues/130)) ([f7cba3b](https://github.com/hyperledger-identus/cloud-agent/commit/f7cba3b18b1e03cd51db144519412fbbbe1585d0))
* **prism-agent:** make didcomm service url configurable ([#173](https://github.com/hyperledger-identus/cloud-agent/issues/173)) ([b162172](https://github.com/hyperledger-identus/cloud-agent/commit/b16217271e38c30cf680e193121baa7122fff67f))

### Reverts

* **connect:** revert wrongly released 1.0.0 ([b5e81ec](https://github.com/hyperledger-identus/cloud-agent/commit/b5e81ec9ec89f2baf9d37a09045e4705c6fb57d1))

## [0.4.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.3.0...prism-agent-v0.4.0) (2022-11-24)

### Features

* **apollo:** add schema registry to the agent using Tapir library. ATL-1334  ([#94](https://github.com/hyperledger-identus/cloud-agent/issues/94)) ([b3cf828](https://github.com/hyperledger-identus/cloud-agent/commit/b3cf828d001f7499c414e9dc559f5152997445e6))
* **pollux:** Verify PresentProof Protocol ([#149](https://github.com/hyperledger-identus/cloud-agent/issues/149)) ([7f0f412](https://github.com/hyperledger-identus/cloud-agent/commit/7f0f412580b1111338015ac8bb16b525e946fed4))

## [0.3.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.2.0...prism-agent-v0.3.0) (2022-11-21)

### Features

* **connect:** Added Connect Lib ([#138](https://github.com/hyperledger-identus/cloud-agent/issues/138)) ([e4d4a41](https://github.com/hyperledger-identus/cloud-agent/commit/e4d4a419a1b736352aa6ec163bdf6ef6d0491640))
* **connect:** removed the unwanted dependencies ([#145](https://github.com/hyperledger-identus/cloud-agent/issues/145)) ([96b0fbc](https://github.com/hyperledger-identus/cloud-agent/commit/96b0fbcd83879840c1b6a46cfeff3604c92ea2a4))
* **mercury:** Add plugin sbt-scoverage ([#133](https://github.com/hyperledger-identus/cloud-agent/issues/133)) ([fcaaeda](https://github.com/hyperledger-identus/cloud-agent/commit/fcaaeda1f8b2b06ae0cc2cf964228686186f6a15))
* **mercury:** add utils methods on issue-credential-protocol ([#131](https://github.com/hyperledger-identus/cloud-agent/issues/131)) ([5a3e2fd](https://github.com/hyperledger-identus/cloud-agent/commit/5a3e2fd411b278f1672777a115233c1f9a408f02))
* **mercury:** ATL-2287 Connection Protocol ([#140](https://github.com/hyperledger-identus/cloud-agent/issues/140)) ([402248b](https://github.com/hyperledger-identus/cloud-agent/commit/402248b3b8553ab7869d70f54ca194510a676e6f))
* **mercury:** Replace zhttp with zio-http ([#137](https://github.com/hyperledger-identus/cloud-agent/issues/137)) ([be6afb3](https://github.com/hyperledger-identus/cloud-agent/commit/be6afb3d8c4f00a1ee0a6429a35c5873302d1cb6))
* **mercury:** Simplify PeerDID ([#132](https://github.com/hyperledger-identus/cloud-agent/issues/132)) ([6d377d8](https://github.com/hyperledger-identus/cloud-agent/commit/6d377d8595c1cdf710625775c06453953a2189a7))
* **pollux:** implement Issue Credential v2 Protocol ([#144](https://github.com/hyperledger-identus/cloud-agent/issues/144)) ([a80702f](https://github.com/hyperledger-identus/cloud-agent/commit/a80702f5b255d8079085a6ec27c87baa6a23ac59)), closes [#92](https://github.com/hyperledger-identus/cloud-agent/issues/92)
* **prism-agent:** implement Issue Credential v2 protocol ([#146](https://github.com/hyperledger-identus/cloud-agent/issues/146)) ([f3cb60e](https://github.com/hyperledger-identus/cloud-agent/commit/f3cb60eb0d4dce73025326d64a012e580f581a7c)), closes [#92](https://github.com/hyperledger-identus/cloud-agent/issues/92)
* **prism-node:** add prism-node scala grpc client generation ([#128](https://github.com/hyperledger-identus/cloud-agent/issues/128)) ([948e864](https://github.com/hyperledger-identus/cloud-agent/commit/948e86423dfe86aaf04ed4ef6ce5eff303a9b5c6))
* **shared:** add base64 helper function ([#135](https://github.com/hyperledger-identus/cloud-agent/issues/135)) ([70555c9](https://github.com/hyperledger-identus/cloud-agent/commit/70555c906121a7bc26e30c15dbf8f9d7721a100a))

## [0.2.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.1.0...prism-agent-v0.2.0) (2022-11-15)

### Features

* **prism-agent:** implement Flyway migrations from castor and pollux library and call on agent startup ([#117](https://github.com/hyperledger-identus/cloud-agent/issues/117)) ([67bd340](https://github.com/hyperledger-identus/cloud-agent/commit/67bd340e25e4a44118398746268ee25f5cf09477))
* **shared:** add Flyway Migrations and expose in ZIO Layer to be used in consuming service ([#115](https://github.com/hyperledger-identus/cloud-agent/issues/115)) ([cd11493](https://github.com/hyperledger-identus/cloud-agent/commit/cd11493aa01494f599fce369918d35edfcd2ce17))
* **shared:** set OAS servers to `k8s-dev.atalaprism.io` and add API Key auth method - also update local docker-compose implementation ([#126](https://github.com/hyperledger-identus/cloud-agent/issues/126)) ([775880a](https://github.com/hyperledger-identus/cloud-agent/commit/775880a032f3a6e6067817f3bab0444c627c6fd5))

## [0.1.0](https://github.com/hyperledger-identus/cloud-agent/compare/prism-agent-v0.0.1...prism-agent-v0.1.0) (2022-11-11)

### Features

* **agent:** [ATL-2005] implement REST API for credential issuance ([#86](https://github.com/hyperledger-identus/cloud-agent/issues/86)) ([7c1f50a](https://github.com/hyperledger-identus/cloud-agent/commit/7c1f50ab99879beed74c5e0bd03aa51709051527))
* **agent:** integrate key-manage into prism-agent server ([#77](https://github.com/hyperledger-identus/cloud-agent/issues/77)) ([4a88ded](https://github.com/hyperledger-identus/cloud-agent/commit/4a88ded408192d03b744309a4ebaf9f9517a9db2))
* define key-management interface (3) ([#71](https://github.com/hyperledger-identus/cloud-agent/issues/71)) ([47dc3cd](https://github.com/hyperledger-identus/cloud-agent/commit/47dc3cd8857971b96a88ae6f9cf0e2163e6cf08e))
* **infra:** [ATL-1889] create local deployment config ([3f381cd](https://github.com/hyperledger-identus/cloud-agent/commit/3f381cdcb7b8abe9082dc4d36a44e89e47b1d0af))
* **infra:** add pre-commit for conventional commits ([e0f8933](https://github.com/hyperledger-identus/cloud-agent/commit/e0f8933c7adb9b8a2e181b88b5ce4563d2f63fa0))
* **iris:** ATL-1791 Implement blockchain syncer functionality ([#49](https://github.com/hyperledger-identus/cloud-agent/issues/49)) ([431b657](https://github.com/hyperledger-identus/cloud-agent/commit/431b6575b8df2f4744285b1c5e2dd56072fa874c))
* **mercury:** [WIP] ATL-1741-Present-Proof-Protocol ([#98](https://github.com/hyperledger-identus/cloud-agent/issues/98)) ([b37b762](https://github.com/hyperledger-identus/cloud-agent/commit/b37b7627377d0e0d6ead496dc537cbf757c875ca))
* **mercury:** add release plugins for sbt ([8ad3be0](https://github.com/hyperledger-identus/cloud-agent/commit/8ad3be012dfcc5e41bc4e6bdff62cb1c413c2dae))
* **mercury:** add semantic-release automation ([44d1c5b](https://github.com/hyperledger-identus/cloud-agent/commit/44d1c5b887938a5adb23e5cef2256154d242c69a))
* **mercury:** AgentCLI for Issue Credential ATL-1740 ([#82](https://github.com/hyperledger-identus/cloud-agent/issues/82)) ([c4068c6](https://github.com/hyperledger-identus/cloud-agent/commit/c4068c62023ef817d80d81a56f90bb7bcb2e7fb3))
* **mercury:** ATL-1740 issue credential ([#64](https://github.com/hyperledger-identus/cloud-agent/issues/64)) ([21fe5bc](https://github.com/hyperledger-identus/cloud-agent/commit/21fe5bc1c0f51c2d039080b8ebb78e226f7815e2))
* **mercury:** Bump Mercury version in Mediator ([#110](https://github.com/hyperledger-identus/cloud-agent/issues/110)) ([3fd370a](https://github.com/hyperledger-identus/cloud-agent/commit/3fd370a7f039b76a23f52bebeb57d5d0de94ea28))
* **mercury:** Coordinate Mediation Protocol ([#33](https://github.com/hyperledger-identus/cloud-agent/issues/33)) ([a3ee0d3](https://github.com/hyperledger-identus/cloud-agent/commit/a3ee0d341a921f39610071eb473d11ceaed0b0e0))
* **mercury:** Expose open api-spec for mediator ([#38](https://github.com/hyperledger-identus/cloud-agent/issues/38)) ([fdad327](https://github.com/hyperledger-identus/cloud-agent/commit/fdad3275d3c2a8c70c43999f5dac78751db0bc70))
* **mercury:** Fix project mercury-mediator ATL-2030 ([#84](https://github.com/hyperledger-identus/cloud-agent/issues/84)) ([1450c30](https://github.com/hyperledger-identus/cloud-agent/commit/1450c30f8f23ec87cd868dbf41e71213e0b4dfbe))
* **mercury:** Fix release ATL-2030 ([#78](https://github.com/hyperledger-identus/cloud-agent/issues/78)) ([93f9e22](https://github.com/hyperledger-identus/cloud-agent/commit/93f9e228529321a19ba6415304e0e2f460f3dddb))
* **mercury:** Jupyter docker workstation and notebooks ([#32](https://github.com/hyperledger-identus/cloud-agent/issues/32)) ([f6b5f77](https://github.com/hyperledger-identus/cloud-agent/commit/f6b5f7708808303f70e54a744073514058c72235))
* **mercury:** Mercury Mailbox Mediator Demo ([#16](https://github.com/hyperledger-identus/cloud-agent/issues/16)) ([27fc9dc](https://github.com/hyperledger-identus/cloud-agent/commit/27fc9dc61c6494dec62b81314b544938fb7c52ca))
* **mercury:** New CI workflow for mercury and fixes ([#57](https://github.com/hyperledger-identus/cloud-agent/issues/57)) ([43ad49a](https://github.com/hyperledger-identus/cloud-agent/commit/43ad49a0c0188dc7db5300c1e32043db10e683e7))
* **mercury:** New CLI Agent for mercury ([#56](https://github.com/hyperledger-identus/cloud-agent/issues/56)) ([5be9c2c](https://github.com/hyperledger-identus/cloud-agent/commit/5be9c2cd0d672ab59f010a5879b745eca65fbf10))
* **mercury:** New docker-compose with ROOTS-ID's mediator ([#26](https://github.com/hyperledger-identus/cloud-agent/issues/26)) ([f6af73f](https://github.com/hyperledger-identus/cloud-agent/commit/f6af73f5e8a7a578dd5e7daf1e6f3d65e98e7e0a))
* **mercury:** Report Problem Protocol ([#21](https://github.com/hyperledger-identus/cloud-agent/issues/21)) ([f5a3711](https://github.com/hyperledger-identus/cloud-agent/commit/f5a3711da0de2ab2af50facb7e69680882920b6d))
* **mercury:** Split Mercury projects ([#73](https://github.com/hyperledger-identus/cloud-agent/issues/73)) ([e88ecd3](https://github.com/hyperledger-identus/cloud-agent/commit/e88ecd3e22649e10f338037b0f3fc2e80a3acb68))
* **mercury:** Use present-proof protocol in AgentCli ([#112](https://github.com/hyperledger-identus/cloud-agent/issues/112)) ([5546190](https://github.com/hyperledger-identus/cloud-agent/commit/55461904f7939fb21cdc0aa17b3d4d179800dba6))
* **pollux:** Add JWT Verifiable Credential Validation ([#83](https://github.com/hyperledger-identus/cloud-agent/issues/83)) [ATL-1999] ([e0869f7](https://github.com/hyperledger-identus/cloud-agent/commit/e0869f7c817bb58f6abf920488645fb38aa470e1))
* **prism-agent; mercury; pollux:** Integrate Mercury into prism-agent ATL-2077; ATL-2076 ([#93](https://github.com/hyperledger-identus/cloud-agent/issues/93)) ([db4b21a](https://github.com/hyperledger-identus/cloud-agent/commit/db4b21ac1d6a2c48af502597779acb82f5e03ac0))
* **shared:** Add environmnet configuration for Iris DB and bump scala version in other components to enable build ([#96](https://github.com/hyperledger-identus/cloud-agent/issues/96)) ([a5b583f](https://github.com/hyperledger-identus/cloud-agent/commit/a5b583f445b7efd31987cf9ca017bc544a877986))

### Bug Fixes

* **architecture:** use explicit version number for Structurizr Lite docker image ([#11](https://github.com/hyperledger-identus/cloud-agent/issues/11)) ([380d024](https://github.com/hyperledger-identus/cloud-agent/commit/380d024b516e1cd0d9b41b7514a3c6d12bc8bb13))
* Delete commons folder ([#46](https://github.com/hyperledger-identus/cloud-agent/issues/46)) ([def88ee](https://github.com/hyperledger-identus/cloud-agent/commit/def88ee476e303775b8e4207047a1cdf46451918))
* **iris:** align type signature ([#72](https://github.com/hyperledger-identus/cloud-agent/issues/72)) ([a19a781](https://github.com/hyperledger-identus/cloud-agent/commit/a19a7814c3fc1e1cc89a861ae3942bf4a5fbad0a))
* **mercury:** use snapshot suffix for prerelease publishing ([#79](https://github.com/hyperledger-identus/cloud-agent/issues/79)) ([a376e59](https://github.com/hyperledger-identus/cloud-agent/commit/a376e59c157f9a27f06021eb5929d161fb28a813))
* **prism-agent:** reuse db connection for background job ([#102](https://github.com/hyperledger-identus/cloud-agent/issues/102)) ([a873090](https://github.com/hyperledger-identus/cloud-agent/commit/a873090fdd2560eb78263060d502946f142b0574))
