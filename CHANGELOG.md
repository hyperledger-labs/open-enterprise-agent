## [1.33.2-snapshot.branch-lo-template.1](https://github.com/hyperledger/identus-cloud-agent/compare/cloud-agent-v1.33.1...cloud-agent-v1.33.2-snapshot.branch-lo-template.1) (2024-05-21)


### Bug Fixes

* dummy commit to trigger a release ([bb1dbab](https://github.com/hyperledger/identus-cloud-agent/commit/bb1dbab446613b53879391aeb404a88ac255e77c))

## [1.33.1](https://github.com/hyperledger/identus-cloud-agent/compare/cloud-agent-v1.33.0...cloud-agent-v1.33.1) (2024-05-21)


### Bug Fixes

* broken link for the cloud agent packages in readme file ([#1032](https://github.com/hyperledger/identus-cloud-agent/issues/1032)) ([92d17c2](https://github.com/hyperledger/identus-cloud-agent/commit/92d17c2b5f82d0ca35313ab51bd90b6f55d2cd87))
* expose new key types in rest api ([#1066](https://github.com/hyperledger/identus-cloud-agent/issues/1066)) ([9ce8d3a](https://github.com/hyperledger/identus-cloud-agent/commit/9ce8d3a8742f86c9a593c705e0f3aa472ff10987))
* rename the folder to identus for vc-jwt ([#1063](https://github.com/hyperledger/identus-cloud-agent/issues/1063)) ([364a5dc](https://github.com/hyperledger/identus-cloud-agent/commit/364a5dc7eb2b9f23b18f3775c207feff02893cbe))


### Performance Improvements

* update ts client in the performance tests, cleanup `println` ([#1041](https://github.com/hyperledger/identus-cloud-agent/issues/1041)) ([7d5ceba](https://github.com/hyperledger/identus-cloud-agent/commit/7d5cebafb34191964acfdf190c743a2ba253e883))

# [1.33.0](https://github.com/hyperledger/identus-cloud-agent/compare/cloud-agent-v1.32.1...cloud-agent-v1.33.0) (2024-05-09)


### Bug Fixes

* integration test ([#1011](https://github.com/hyperledger/identus-cloud-agent/issues/1011)) ([d674f31](https://github.com/hyperledger/identus-cloud-agent/commit/d674f3162be44ba05d50b305be4838525d982706))


### Features

* rename `prism-agent` to `cloud-agent` ([#1019](https://github.com/hyperledger/identus-cloud-agent/issues/1019)) ([74560da](https://github.com/hyperledger/identus-cloud-agent/commit/74560dabf59dac15ccd086edb7a77d9e5055621e))

## [1.32.1](https://github.com/hyperledger/identus-cloud-agent/compare/cloud-agent-v1.32.0...cloud-agent-v1.32.1) (2024-05-07)


### Bug Fixes

* expose pg_admin port on the localhost interface only ([#957](https://github.com/hyperledger/identus-cloud-agent/issues/957)) ([73674b5](https://github.com/hyperledger/identus-cloud-agent/commit/73674b5da6a41c4972ac3c45005ce768608b558e))
* Fix OneOf OpenAPI Serialization Issue ([#1010](https://github.com/hyperledger/identus-cloud-agent/issues/1010)) ([393c296](https://github.com/hyperledger/identus-cloud-agent/commit/393c29654b8d3d53071f0d2932a16ff81688ece6))
* remove prism-crypto dependency ([#1015](https://github.com/hyperledger/identus-cloud-agent/issues/1015)) ([46e594c](https://github.com/hyperledger/identus-cloud-agent/commit/46e594c21bdb43d78f41be6c803ad8b80dc89504))
* update open-api-spec and generator script and package.json ([#990](https://github.com/hyperledger/identus-cloud-agent/issues/990)) ([88c1b5e](https://github.com/hyperledger/identus-cloud-agent/commit/88c1b5eadf62ad0efcd4ee53b793bb08cce9667f))

# [1.32.0](https://github.com/hyperledger/identus-cloud-agent/compare/cloud-agent-v1.31.0...cloud-agent-v1.32.0) (2024-04-26)


### Bug Fixes

* add shared-crypto module and apollo wrapper for other key types ([#958](https://github.com/hyperledger/identus-cloud-agent/issues/958)) ([7eaa66c](https://github.com/hyperledger/identus-cloud-agent/commit/7eaa66c51904e58c309fd3bb0a8e8864c7902cb9))
* Check purpose of the keys ([#968](https://github.com/hyperledger/identus-cloud-agent/issues/968)) ([4b8e48d](https://github.com/hyperledger/identus-cloud-agent/commit/4b8e48d238b751ee8526e1440e8a0515f6b62de5))
* Integration Test ([#974](https://github.com/hyperledger/identus-cloud-agent/issues/974)) ([847eb2f](https://github.com/hyperledger/identus-cloud-agent/commit/847eb2f56ba2766ed28f6484391cbd9b3202fbe5))
* **prism-agent:** add missing 'PresentationVerificationFailed' status to REST API response enum ([#948](https://github.com/hyperledger/identus-cloud-agent/issues/948)) ([9a38cc9](https://github.com/hyperledger/identus-cloud-agent/commit/9a38cc97c18d26f13efa1c9535de1df003547a2b))
* use apollo for secp256k1 in shared-crypto ([#971](https://github.com/hyperledger/identus-cloud-agent/issues/971)) ([dd5e20b](https://github.com/hyperledger/identus-cloud-agent/commit/dd5e20bdedd004c2e0a81383d10b5eee3ae03788))


### Features

* add sample maintainers.md ([#878](https://github.com/hyperledger/identus-cloud-agent/issues/878)) ([c6a41ed](https://github.com/hyperledger/identus-cloud-agent/commit/c6a41edc5466312da76283d37f12406d79449a7d))
* **agent:** verification API integration test + documentation ATL-6775 ([#986](https://github.com/hyperledger/identus-cloud-agent/issues/986)) ([9308b21](https://github.com/hyperledger/identus-cloud-agent/commit/9308b21e46d267265c7fcdbfc88b78ddd5ae6558))
* Align the repo with new name identus-cloud-agent ([#973](https://github.com/hyperledger/identus-cloud-agent/issues/973)) ([9fc7bb0](https://github.com/hyperledger/identus-cloud-agent/commit/9fc7bb07cac9aae1db8d389ec8831403f106d612))
* Configurations load improvement ([#954](https://github.com/hyperledger/identus-cloud-agent/issues/954)) ([dfb7577](https://github.com/hyperledger/identus-cloud-agent/commit/dfb75778f925a615aaec815241d964014d236777))
* **connect:** ATL-6599 Use ZIO Failures and Defects effectively + RFC-9457 in connect ([#927](https://github.com/hyperledger/identus-cloud-agent/issues/927)) ([eb898e0](https://github.com/hyperledger/identus-cloud-agent/commit/eb898e068f768507d6979a5d9bab35ef7ad4a045))
* key management for Ed25519 and X25519 ([#966](https://github.com/hyperledger/identus-cloud-agent/issues/966)) ([a0f6819](https://github.com/hyperledger/identus-cloud-agent/commit/a0f6819bb80d87c13f903d7dc1b67cb08b4687db))
* Vc Verification Api ([#975](https://github.com/hyperledger/identus-cloud-agent/issues/975)) ([f0a1f2c](https://github.com/hyperledger/identus-cloud-agent/commit/f0a1f2c1aaafb636cabcb84c599d9deaa90fd373))

# [1.31.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.30.1...prism-agent-v1.31.0) (2024-03-20)


### Bug Fixes

* add Anoncreds Integration Test ([#923](https://github.com/hyperledger/identus-cloud-agent/issues/923)) ([27a157f](https://github.com/hyperledger/identus-cloud-agent/commit/27a157fb5a83a33704230b35eca80f297eec9a85))
* align keycloak version ([#936](https://github.com/hyperledger/identus-cloud-agent/issues/936)) ([c920fd6](https://github.com/hyperledger/identus-cloud-agent/commit/c920fd661d31b56466497e462fd0139755290b33))
* anoncred test ([#940](https://github.com/hyperledger/identus-cloud-agent/issues/940)) ([bb5ead1](https://github.com/hyperledger/identus-cloud-agent/commit/bb5ead1de3b6d2c3fae0019d50a5310b37a2eab3))
* **pollux:** function that allocates status list credential does not work correctly in multi threaded environment  ([#941](https://github.com/hyperledger/identus-cloud-agent/issues/941)) ([ecc3c01](https://github.com/hyperledger/identus-cloud-agent/commit/ecc3c019749a6b370771bcf62f89ea2599a521ac))
* **pollux:** Undo edit migration for revocation status lists ([#937](https://github.com/hyperledger/identus-cloud-agent/issues/937)) ([7f7585f](https://github.com/hyperledger/identus-cloud-agent/commit/7f7585f47ee197db162c75f7d118a87b99cc7d06))
* **prism-agent:** add validation for endpoint url ([#919](https://github.com/hyperledger/identus-cloud-agent/issues/919)) ([0402a87](https://github.com/hyperledger/identus-cloud-agent/commit/0402a8778eda839521c55a127934fba41c7b79ad))


### Features

* add revocation for JWT credentials ([#934](https://github.com/hyperledger/identus-cloud-agent/issues/934)) ([88b7fa5](https://github.com/hyperledger/identus-cloud-agent/commit/88b7fa5c6cd92002ef355311eec6e30b63ab1dd6))
* Fix Update Schema and CredentialDef on Receive Credential ([#920](https://github.com/hyperledger/identus-cloud-agent/issues/920)) ([acbba3c](https://github.com/hyperledger/identus-cloud-agent/commit/acbba3ce92ee9e16893b4978c4a9ec4ce0757d53))

## [1.30.1](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.30.0...prism-agent-v1.30.1) (2024-03-06)


### Bug Fixes

* allow configurable path convention for vault secrets ([#918](https://github.com/hyperledger/identus-cloud-agent/issues/918)) ([234a272](https://github.com/hyperledger/identus-cloud-agent/commit/234a2725614b05466391894f248c7175fb62c5b6))
* integration test ([#915](https://github.com/hyperledger/identus-cloud-agent/issues/915)) ([320ab6a](https://github.com/hyperledger/identus-cloud-agent/commit/320ab6a876606eb68f48fe7d78983b4e044b5084))

# [1.30.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.29.0...prism-agent-v1.30.0) (2024-03-01)


### Features

* **agent:** make the connection pool size configurable, fixes [#913](https://github.com/hyperledger/identus-cloud-agent/issues/913) ([#914](https://github.com/hyperledger/identus-cloud-agent/issues/914)) ([375fe0f](https://github.com/hyperledger/identus-cloud-agent/commit/375fe0f8ee042246aed37f40cbeb8f2042c99958))

# [1.29.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.28.0...prism-agent-v1.29.0) (2024-02-28)


### Bug Fixes

* correct the config environment variable name ([#905](https://github.com/hyperledger/identus-cloud-agent/issues/905)) ([d86436c](https://github.com/hyperledger/identus-cloud-agent/commit/d86436cbc58571b4167411643623f3ba975550ad))


### Features

* ZKP verification ([#792](https://github.com/hyperledger/identus-cloud-agent/issues/792)) ([ab1ab64](https://github.com/hyperledger/identus-cloud-agent/commit/ab1ab648b10a82b39d4bdde6e2c9693c8f7506b8))

# [1.28.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.27.0...prism-agent-v1.28.0) (2024-02-20)


### Bug Fixes

* **prism-agent:** increase http timeout communication channel closing… ([#901](https://github.com/hyperledger/identus-cloud-agent/issues/901)) ([8d3f29d](https://github.com/hyperledger/identus-cloud-agent/commit/8d3f29ddd830fe102d4bf25a0af8734730c80151))
* re-enable logging with SLF4J and add traceId ([#869](https://github.com/hyperledger/identus-cloud-agent/issues/869)) ([8f6af25](https://github.com/hyperledger/identus-cloud-agent/commit/8f6af25a8eafd27d5017096da64f89188354a2ca))
* remove oas schema format for empty repsonse body ([#902](https://github.com/hyperledger/identus-cloud-agent/issues/902)) ([5f2bb08](https://github.com/hyperledger/identus-cloud-agent/commit/5f2bb0872a156c9223ab56efbd47e812967ff582))


### Features

* add credential def performance test ([#865](https://github.com/hyperledger/identus-cloud-agent/issues/865)) ([95064d6](https://github.com/hyperledger/identus-cloud-agent/commit/95064d617dda7d45916fbdddf20a544eea4acf4a))
* **pollux:** add Json VC schema meta validation ([#892](https://github.com/hyperledger/identus-cloud-agent/issues/892)) ([19c42b1](https://github.com/hyperledger/identus-cloud-agent/commit/19c42b10188d1e0242bdceb1f89b6410dcc05353))

# [1.27.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.26.0...prism-agent-v1.27.0) (2024-02-08)


### Bug Fixes

* remove hard code did:example:* ([#882](https://github.com/hyperledger/identus-cloud-agent/issues/882)) ([321faf5](https://github.com/hyperledger/identus-cloud-agent/commit/321faf5791b05f0c24dd6ed96f155aa65d06477d))


### Features

* support vault AppRole authentication ([#884](https://github.com/hyperledger/identus-cloud-agent/issues/884)) ([441f878](https://github.com/hyperledger/identus-cloud-agent/commit/441f878f0b573f350ffed14de0df164e1e260122))

# [1.26.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.25.0...prism-agent-v1.26.0) (2024-02-06)


### Bug Fixes

* **prism-agent:**  update didcomm peerdid library to support latest spec ([#877](https://github.com/hyperledger/identus-cloud-agent/issues/877)) ([0c42a62](https://github.com/hyperledger/identus-cloud-agent/commit/0c42a622143e35c439ae83cc3bb746515ce7401b))


### Features

* interoperable schema changes ([#870](https://github.com/hyperledger/identus-cloud-agent/issues/870)) ([de49a93](https://github.com/hyperledger/identus-cloud-agent/commit/de49a9328524b32d714301f2b7961d5cd85b23c3))

# [1.25.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.24.0...prism-agent-v1.25.0) (2024-01-25)


### Features

* **prism-agent:** add JWT auth support for agent-admin role ([#840](https://github.com/hyperledger/identus-cloud-agent/issues/840)) ([3ccd56e](https://github.com/hyperledger/identus-cloud-agent/commit/3ccd56efadbbb3ea70e7ca63e9eb89564a83c02f))
* upgrade ZIO http client to improve performance ([#850](https://github.com/hyperledger/identus-cloud-agent/issues/850)) ([7aa9b4c](https://github.com/hyperledger/identus-cloud-agent/commit/7aa9b4c27f92b169c72a68cd4bb8f4afb63943d4))

# [1.24.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.23.0...prism-agent-v1.24.0) (2023-12-21)


### Features

* add configuration for gRPC usePlainText (enable TLS for gRPC) ([#823](https://github.com/hyperledger/identus-cloud-agent/issues/823)) ([b871bb5](https://github.com/hyperledger/identus-cloud-agent/commit/b871bb5e8eeeb71b3f22c38609ae8f1ff424016c))

# [1.23.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.22.0...prism-agent-v1.23.0) (2023-12-20)


### Bug Fixes

* Change resource defaults for postgres ([#827](https://github.com/hyperledger/identus-cloud-agent/issues/827)) ([87809c4](https://github.com/hyperledger/identus-cloud-agent/commit/87809c4b4d6a3baf0afa37a2cf7ddf6c41a80eb6))
* correct OAS example ([#816](https://github.com/hyperledger/identus-cloud-agent/issues/816)) ([b1384b3](https://github.com/hyperledger/identus-cloud-agent/commit/b1384b38524060f3cfa6df39afaddcce26a5514e))
* Swithing to startupProbe from Readiness ([#821](https://github.com/hyperledger/identus-cloud-agent/issues/821)) ([22a78ec](https://github.com/hyperledger/identus-cloud-agent/commit/22a78ec09ccc84b5d5c03e8f07ff3a14c654cf2b))


### Features

* complete the integration with anoncred and fixes ([#820](https://github.com/hyperledger/identus-cloud-agent/issues/820)) ([15ff710](https://github.com/hyperledger/identus-cloud-agent/commit/15ff710c4a68c5f282e07b23098e362825fdb3b9))
* Liveness, and readiness probes ([#817](https://github.com/hyperledger/identus-cloud-agent/issues/817)) ([6e18666](https://github.com/hyperledger/identus-cloud-agent/commit/6e18666f51cca2d0f151b070c59049bcf005a450))
* Tune postgresql - helm chart ([#822](https://github.com/hyperledger/identus-cloud-agent/issues/822)) ([5fcd9fb](https://github.com/hyperledger/identus-cloud-agent/commit/5fcd9fb6301a5c7b1296d60e4b6fa3385ffe6727))

# [1.22.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.21.1...prism-agent-v1.22.0) (2023-12-14)


### Bug Fixes

* correct OAS examples ([#810](https://github.com/hyperledger/identus-cloud-agent/issues/810)) ([a0720dc](https://github.com/hyperledger/identus-cloud-agent/commit/a0720dcbaf10370dcacc1b5102df13929b40dfdb))


### Features

* Consumer restricition parametarization  ([#814](https://github.com/hyperledger/identus-cloud-agent/issues/814)) ([e039576](https://github.com/hyperledger/identus-cloud-agent/commit/e039576fc0e285b80b2966c032ed91b9a8f26f60))

## [1.21.1](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.21.0...prism-agent-v1.21.1) (2023-12-12)


### Bug Fixes

* Option to disable apisix key auth ([#813](https://github.com/hyperledger/identus-cloud-agent/issues/813)) ([f163682](https://github.com/hyperledger/identus-cloud-agent/commit/f1636824047c0d03ce0790ede54e3a12d63dd787))

# [1.21.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.20.1...prism-agent-v1.21.0) (2023-12-12)


### Features

* env vars support through values file ([#811](https://github.com/hyperledger/identus-cloud-agent/issues/811)) ([2486dde](https://github.com/hyperledger/identus-cloud-agent/commit/2486dde9b0682504a02ad031b3e7498b2fa2ce17))

## [1.20.1](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.20.0...prism-agent-v1.20.1) (2023-12-06)


### Bug Fixes

* tolerations and nodeAffinity for k8s ([#808](https://github.com/hyperledger/identus-cloud-agent/issues/808)) ([7934fa4](https://github.com/hyperledger/identus-cloud-agent/commit/7934fa402ba86af6d8430208f1844fbd6ccda1bd))

# [1.20.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.19.1...prism-agent-v1.20.0) (2023-12-05)


### Features

* add nodeAffinity, tolerations, and resources to k8s deployment ([#804](https://github.com/hyperledger/identus-cloud-agent/issues/804)) ([22407a3](https://github.com/hyperledger/identus-cloud-agent/commit/22407a3103eff73d87ead9a8122f078845c11d95))

## [1.19.1](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.19.0...prism-agent-v1.19.1) (2023-11-29)


### Bug Fixes

* change admin auth priority and improve auth error message ([#800](https://github.com/hyperledger/identus-cloud-agent/issues/800)) ([32d4340](https://github.com/hyperledger/identus-cloud-agent/commit/32d43401a69c339f54380bd8d5dfe2fa383cb8d7))

# [1.19.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.18.0...prism-agent-v1.19.0) (2023-11-21)


### Bug Fixes

* check for active RLS on db application user ([#775](https://github.com/hyperledger/identus-cloud-agent/issues/775)) ([a792f43](https://github.com/hyperledger/identus-cloud-agent/commit/a792f43eaae0ec2cd30db2ea3308deded7a1a935))
* enable keycloak with pre-configured agent in helm chart ([#791](https://github.com/hyperledger/identus-cloud-agent/issues/791)) ([9a6e512](https://github.com/hyperledger/identus-cloud-agent/commit/9a6e5123e07462db66017439e8e434315af7c0f4))
* explicitly define transitive dependencies that were unresolvable ([#790](https://github.com/hyperledger/identus-cloud-agent/issues/790)) ([0647829](https://github.com/hyperledger/identus-cloud-agent/commit/0647829af813913aebd0dd3d703db7e363d44369))
* **pollux:** V16 migration is failing to add FK constraint because of type mismatch ([#782](https://github.com/hyperledger/identus-cloud-agent/issues/782)) ([c87beb0](https://github.com/hyperledger/identus-cloud-agent/commit/c87beb0478d4b3d54709e09597c42c23878d101e))
* **prism-agent:** more descriptive error response for validateDID in issue flow ([#783](https://github.com/hyperledger/identus-cloud-agent/issues/783)) ([b99a737](https://github.com/hyperledger/identus-cloud-agent/commit/b99a73718a06f4b97d933ba2e3220593f8d4e825))
* **prism-agent:** perform percent encoding on auth header for token introspection request ([#780](https://github.com/hyperledger/identus-cloud-agent/issues/780)) ([03d43c9](https://github.com/hyperledger/identus-cloud-agent/commit/03d43c98d8ab64e5b47830d95a6356f9d6dd1b82))


### Features

* Accept goal and goalCode to create connection invitation  ([#785](https://github.com/hyperledger/identus-cloud-agent/issues/785)) ([71c776b](https://github.com/hyperledger/identus-cloud-agent/commit/71c776baa2caf3ca610508dba805f037fd7d6e29))
* **docs:** ADR for revocation status list expansion strategy ([#773](https://github.com/hyperledger/identus-cloud-agent/issues/773)) ([7ad6427](https://github.com/hyperledger/identus-cloud-agent/commit/7ad64277acb2bffe12524c4bfb68f687689b5b2e))
* Keycloak container support with clients and PermissionManagement service ([#755](https://github.com/hyperledger/identus-cloud-agent/issues/755)) ([a1846aa](https://github.com/hyperledger/identus-cloud-agent/commit/a1846aaa84202b55d48ea8556aad8cbbb8260f4d))
* **pollux:** Add migrations needed for JWT revocation ([#778](https://github.com/hyperledger/identus-cloud-agent/issues/778)) ([471956e](https://github.com/hyperledger/identus-cloud-agent/commit/471956e92893a7237cabca2fb065adb417678d37))
* **prism-agent:** add multi-tenant wallet self-service capability ([#779](https://github.com/hyperledger/identus-cloud-agent/issues/779)) ([f2e74cd](https://github.com/hyperledger/identus-cloud-agent/commit/f2e74cd1957e7d76f6dccadd02b1ca5b794d02b1))

# [1.18.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.17.0...prism-agent-v1.18.0) (2023-10-24)


### Bug Fixes

*  all performance tests run succesfully, add group thresholds ([#750](https://github.com/hyperledger/identus-cloud-agent/issues/750)) ([5204838](https://github.com/hyperledger/identus-cloud-agent/commit/520483836e5b572e8aeeecd28f4bbe7cc668c3d9))


### Features

*  presentation API refactor ([#765](https://github.com/hyperledger/identus-cloud-agent/issues/765)) ([045d829](https://github.com/hyperledger/identus-cloud-agent/commit/045d8298f8865baeb13e243ed058e8e440b3f496))
* add new auth params ([#762](https://github.com/hyperledger/identus-cloud-agent/issues/762)) ([b8bfb86](https://github.com/hyperledger/identus-cloud-agent/commit/b8bfb867061c58fc12987b5405f561e8f10cb718))
* disable cors by default ([#747](https://github.com/hyperledger/identus-cloud-agent/issues/747)) ([1dd8c8b](https://github.com/hyperledger/identus-cloud-agent/commit/1dd8c8b0e9b0d2593bd1c17a95bf013192a64532))
* migrate docker image of the agent to Java 21 ([#758](https://github.com/hyperledger/identus-cloud-agent/issues/758)) ([d36dbf0](https://github.com/hyperledger/identus-cloud-agent/commit/d36dbf0dfbf45b64185e5b54aba0444d6e1ada88))
* **prism-agent:** add keycloak authorization support to endpoints ([#753](https://github.com/hyperledger/identus-cloud-agent/issues/753)) ([3e7534f](https://github.com/hyperledger/identus-cloud-agent/commit/3e7534ff1a75e9ecaa0c2b670c1c158890021f8d))

# [1.17.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.16.4...prism-agent-v1.17.0) (2023-10-14)


### Bug Fixes

* change repository and name for rest api clients ([#745](https://github.com/hyperledger/identus-cloud-agent/issues/745)) ([0f84e28](https://github.com/hyperledger/identus-cloud-agent/commit/0f84e28c3f2800c1d73353e40620a840fbb6b93a))
* improve performance for background jobs in multitenancy mode ([#749](https://github.com/hyperledger/identus-cloud-agent/issues/749)) ([17def3f](https://github.com/hyperledger/identus-cloud-agent/commit/17def3f67c1eb687560aee844aba6ff2a0bd4137))
* **prism-agent:** agent should read DIDComm port from config ([#757](https://github.com/hyperledger/identus-cloud-agent/issues/757)) ([cda908c](https://github.com/hyperledger/identus-cloud-agent/commit/cda908c87cee562e6c044aa405aa82bd510cc74e))
* **prism-agent:** configure APISIX to return CORS headers from Prism Agent endpoints ([#746](https://github.com/hyperledger/identus-cloud-agent/issues/746)) ([a579aa9](https://github.com/hyperledger/identus-cloud-agent/commit/a579aa95ea5c0c4950cb64b8b9adb1f56bb87eb2))
* **prism-agent:** fix docker env variables interpolation issue ([#751](https://github.com/hyperledger/identus-cloud-agent/issues/751)) ([110eb2d](https://github.com/hyperledger/identus-cloud-agent/commit/110eb2df9590412b35997152d526c599edb8e7af))
* **prism-agent:** return relevant errors on offer creation ([#754](https://github.com/hyperledger/identus-cloud-agent/issues/754)) ([d36533f](https://github.com/hyperledger/identus-cloud-agent/commit/d36533fe538812c9e3647bcc2383700173e4b1b7))
* prohibit tenants to use equal or revoked api keys ([#742](https://github.com/hyperledger/identus-cloud-agent/issues/742)) ([4b10c3a](https://github.com/hyperledger/identus-cloud-agent/commit/4b10c3af931722a683bf55062297c3dfa1e38046))
* upgrade vault and quill versions ([#739](https://github.com/hyperledger/identus-cloud-agent/issues/739)) ([c140857](https://github.com/hyperledger/identus-cloud-agent/commit/c140857df97d56ab750ec186962e5fe2bb6a6717))


### Features

* **prism-agent:** check issuing DID validity when creating a VC offer + return 'metaRetries' ([#740](https://github.com/hyperledger/identus-cloud-agent/issues/740)) ([f2e2fd3](https://github.com/hyperledger/identus-cloud-agent/commit/f2e2fd3d0397422be40b11644f8b84ddd3c6985f))
* **prism-agent:** implement AnonCreds issuance flow ([#693](https://github.com/hyperledger/identus-cloud-agent/issues/693)) ([9165a6f](https://github.com/hyperledger/identus-cloud-agent/commit/9165a6f8fc0a11bd6c19b0bfd4dd4217ea3194d9))

## [1.16.4](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.16.3...prism-agent-v1.16.4) (2023-09-29)


### Bug Fixes

* Integration flow ATL-5777 ([#738](https://github.com/hyperledger/identus-cloud-agent/issues/738)) ([7cf927c](https://github.com/hyperledger/identus-cloud-agent/commit/7cf927cc267460cc1708e424c0a63ae96689a42a))

## [1.16.3](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.16.2...prism-agent-v1.16.3) (2023-09-28)


### Bug Fixes

* Adding labels [skip ci] ([#737](https://github.com/hyperledger/identus-cloud-agent/issues/737)) ([5182098](https://github.com/hyperledger/identus-cloud-agent/commit/5182098f7bc6db479ee64d7133389732a38d174a))

## [1.16.2](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.16.1...prism-agent-v1.16.2) (2023-09-28)


### Bug Fixes

* Changing yq command [skip ci] ATL-5777 ([#736](https://github.com/hyperledger/identus-cloud-agent/issues/736)) ([01bdfa7](https://github.com/hyperledger/identus-cloud-agent/commit/01bdfa74056d983bc1fd494c99b1bab8496dc62f))
* Integration flow ([#734](https://github.com/hyperledger/identus-cloud-agent/issues/734)) ([c45a9eb](https://github.com/hyperledger/identus-cloud-agent/commit/c45a9ebf0150245dfa5ebdc2eda94aa1e0fea8f3))
* Renaming values.yml [skip ci] ATL-5777  ([#735](https://github.com/hyperledger/identus-cloud-agent/issues/735)) ([bcd73c3](https://github.com/hyperledger/identus-cloud-agent/commit/bcd73c310a1c033400f83cfa266cbe0aa304217a))

## [1.16.1](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.16.0...prism-agent-v1.16.1) (2023-09-27)


### Bug Fixes

* Adding localhost environment variable in run.sh script for local development ([#728](https://github.com/hyperledger/identus-cloud-agent/issues/728)) ([1a904a6](https://github.com/hyperledger/identus-cloud-agent/commit/1a904a6f72676fb89f87dd2da14c01d291371f8c))
* correct typo on sts header (dmax -> max) ([#726](https://github.com/hyperledger/identus-cloud-agent/issues/726)) ([2c5bc51](https://github.com/hyperledger/identus-cloud-agent/commit/2c5bc51fc66b2c62a7c8ba7e25944704c335253f))
* **prism-agent:** introduce generic secret store for CD ([#727](https://github.com/hyperledger/identus-cloud-agent/issues/727)) ([3d4aacd](https://github.com/hyperledger/identus-cloud-agent/commit/3d4aacdd9a7f66f2f656d3c31b3f8202cc37c51b))
* Separate config for integration flow ATL-5777 ([#731](https://github.com/hyperledger/identus-cloud-agent/issues/731)) ([9e0e2de](https://github.com/hyperledger/identus-cloud-agent/commit/9e0e2de77a25166f019f78356b2e98b60da7b3e1))
* Separate config for integration flow ATL-5777 ([#733](https://github.com/hyperledger/identus-cloud-agent/issues/733)) ([8380ccc](https://github.com/hyperledger/identus-cloud-agent/commit/8380cccea0eee17c090928b1ae36b877a822177d))

# [1.16.0](https://github.com/hyperledger/identus-cloud-agent/compare/prism-agent-v1.15.0...prism-agent-v1.16.0) (2023-09-15)


### Bug Fixes

* change attribute for appuser to login ([#721](https://github.com/hyperledger/identus-cloud-agent/issues/721)) ([a0e0a74](https://github.com/hyperledger/identus-cloud-agent/commit/a0e0a7412172a7cc2010c39c8ee106319e710986))
* entity create and update operation failures if the walletId does… ([#718](https://github.com/hyperledger/identus-cloud-agent/issues/718)) ([4fe6677](https://github.com/hyperledger/identus-cloud-agent/commit/4fe66773a5aad4dc2808dad036c54c4660b3a855))
* **prism-agent:** define db app user privileges before app starts ([#722](https://github.com/hyperledger/identus-cloud-agent/issues/722)) ([8039654](https://github.com/hyperledger/identus-cloud-agent/commit/803965482e2634d488d2f4f364b041917be514a5))
* **prism-agent:** incorrect present proof metric name and remove connectionID from flow metrics ([#720](https://github.com/hyperledger/identus-cloud-agent/issues/720)) ([52e31b0](https://github.com/hyperledger/identus-cloud-agent/commit/52e31b0721d959fa53c8c49a39288b7c50d4582d))
* **prism-agent:** refine multi-tenant error response and validations ([#719](https://github.com/hyperledger/identus-cloud-agent/issues/719)) ([1f9ede3](https://github.com/hyperledger/identus-cloud-agent/commit/1f9ede395c4469bf26b167a6430ad42ea7cde301))
* **prism-agent:** validate application config during startup ([#712](https://github.com/hyperledger/identus-cloud-agent/issues/712)) ([46fd69b](https://github.com/hyperledger/identus-cloud-agent/commit/46fd69bc2416c72dd457b29f06dd181cf65f52a0))
* use postgres application user ([#717](https://github.com/hyperledger/identus-cloud-agent/issues/717)) ([63403a5](https://github.com/hyperledger/identus-cloud-agent/commit/63403a5d64860d4683ebaab00a86eec0578a21c0))


### Features

* **prism-agent:** Metrics for verification flow ([#714](https://github.com/hyperledger/identus-cloud-agent/issues/714)) ([8bea26e](https://github.com/hyperledger/identus-cloud-agent/commit/8bea26e955987e1543984e090bedad17a7863268))

# [1.15.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.14.2...prism-agent-v1.15.0) (2023-09-12)


### Features

* add security headers in helm-chart apisix route ([#697](https://github.com/input-output-hk/atala-prism-building-blocks/issues/697)) ([7f7e0a4](https://github.com/input-output-hk/atala-prism-building-blocks/commit/7f7e0a4b7709c9eb0dbfc0557ed68648a98e5756))
* **prism-agent:** add multi-tenancy capability ([#696](https://github.com/input-output-hk/atala-prism-building-blocks/issues/696)) ([b6c9a40](https://github.com/input-output-hk/atala-prism-building-blocks/commit/b6c9a40733af1a80c2fc7c17650d1f9ca53c21da))

## [1.14.2](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.14.1...prism-agent-v1.14.2) (2023-09-06)


### Bug Fixes

* add missing leading '/' in rewrite rule target ([#692](https://github.com/input-output-hk/atala-prism-building-blocks/issues/692)) ([f2be228](https://github.com/input-output-hk/atala-prism-building-blocks/commit/f2be22895c893525b50a0848d2951e668c5fe688))

## [1.14.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.14.0...prism-agent-v1.14.1) (2023-09-06)


### Bug Fixes

* APISIX config route and rule names too long ([#691](https://github.com/input-output-hk/atala-prism-building-blocks/issues/691)) ([bef008e](https://github.com/input-output-hk/atala-prism-building-blocks/commit/bef008ecb8f07a7e92cba1ba009a343af7a71adb))

# [1.14.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.13.0...prism-agent-v1.14.0) (2023-09-06)


### Bug Fixes

* change private sdk 1.4 to public maven ([#685](https://github.com/input-output-hk/atala-prism-building-blocks/issues/685)) ([128bcac](https://github.com/input-output-hk/atala-prism-building-blocks/commit/128bcac5b7006b485ea3dc9272fde29d159f1a03))
* **prism-agent:** update invitation expiration on connection request ([#687](https://github.com/input-output-hk/atala-prism-building-blocks/issues/687)) ([1a1702f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/1a1702fc4e62b4a03a4e4ee32ac7419ea67a4ea1))


### Features

* **prism-agent:** add syncwave to certificate to fix race condition with tls ([#686](https://github.com/input-output-hk/atala-prism-building-blocks/issues/686)) ([854dcf9](https://github.com/input-output-hk/atala-prism-building-blocks/commit/854dcf96c48defcb1f44062c3dfd88555dcaebe1))

# [1.13.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.12.0...prism-agent-v1.13.0) (2023-09-06)


### Bug Fixes

* another indentation in apisixroute ([#683](https://github.com/input-output-hk/atala-prism-building-blocks/issues/683)) ([d7c5e52](https://github.com/input-output-hk/atala-prism-building-blocks/commit/d7c5e526c2b40897b3e26a8272468f21fc1dd81f))
* correct vault path ([#678](https://github.com/input-output-hk/atala-prism-building-blocks/issues/678)) ([9426e7f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/9426e7f069e2dd72c18b1e09f7b34b2d37854771))
* CredentialOffer not following spec ([#569](https://github.com/input-output-hk/atala-prism-building-blocks/issues/569)) ([3d479b9](https://github.com/input-output-hk/atala-prism-building-blocks/commit/3d479b9fc0c0bdb0aa78b8c4e2edc8d287a0b6d9))
* indentation in apisixroute ([#682](https://github.com/input-output-hk/atala-prism-building-blocks/issues/682)) ([6eec8ba](https://github.com/input-output-hk/atala-prism-building-blocks/commit/6eec8ba6a32ce088eaaeafe953c4ff554d4765ab))
* **prism-agent:** fix added the InvitationGenerated state to the conn… ([#684](https://github.com/input-output-hk/atala-prism-building-blocks/issues/684)) ([7fdffe3](https://github.com/input-output-hk/atala-prism-building-blocks/commit/7fdffe3990ea08bd6dad5f9d2124146cb8efcff4))
* **prism-agent:** make resolve did representation content type work ([#679](https://github.com/input-output-hk/atala-prism-building-blocks/issues/679)) ([fd417d9](https://github.com/input-output-hk/atala-prism-building-blocks/commit/fd417d9bdac0db98bc3de7a84e4d3277aef3c403))


### Features

* **prism-agent:** metrics for issuance flow ([#669](https://github.com/input-output-hk/atala-prism-building-blocks/issues/669)) ([20315ae](https://github.com/input-output-hk/atala-prism-building-blocks/commit/20315aedea3c8c2953cfd5ee391feb10fbc1146c))

# [1.12.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.11.0...prism-agent-v1.12.0) (2023-08-31)


### Bug Fixes

* **prism-agent:** invitation expiry configuration and new state ([#655](https://github.com/input-output-hk/atala-prism-building-blocks/issues/655)) ([c61999d](https://github.com/input-output-hk/atala-prism-building-blocks/commit/c61999dd2a256401c30d29b842f0092f4968c6ed))


### Features

* add anoncreds credential definition rest api ([#624](https://github.com/input-output-hk/atala-prism-building-blocks/issues/624)) ([99e338a](https://github.com/input-output-hk/atala-prism-building-blocks/commit/99e338af6dc1ab2b4b42f4b1bee2a917ccb77b4c))
* allow external API keys to be defined for an agent ([#643](https://github.com/input-output-hk/atala-prism-building-blocks/issues/643)) ([756dea7](https://github.com/input-output-hk/atala-prism-building-blocks/commit/756dea707b1ced9de800cdabfded6dfc100e340e))
* ATL-5571 Generalized Vault to Store Json ([#650](https://github.com/input-output-hk/atala-prism-building-blocks/issues/650)) ([ebf0328](https://github.com/input-output-hk/atala-prism-building-blocks/commit/ebf0328cfb5107954766fe93ffc6b42f4e5a4cb0))
* ATL-5574 Prime Anoncred Lib ([#652](https://github.com/input-output-hk/atala-prism-building-blocks/issues/652)) ([70b2f16](https://github.com/input-output-hk/atala-prism-building-blocks/commit/70b2f16beecdef7eeeabb18f1b25244046ba5a65))
* ATL-5575 Generalize and Streamline Json Schema SerDes logic ([#653](https://github.com/input-output-hk/atala-prism-building-blocks/issues/653)) ([eb4f8f4](https://github.com/input-output-hk/atala-prism-building-blocks/commit/eb4f8f488bcef421e20f770669dfff99f4c1dd98))

# [1.11.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.10.0...prism-agent-v1.11.0) (2023-08-21)


### Bug Fixes

* **prism-agenet:** Remove connection ID from metrics in connection flow ([#635](https://github.com/input-output-hk/atala-prism-building-blocks/issues/635)) ([515f92f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/515f92f67f6ccd9ae5414d1324ebb769c43d5017))


### Features

* **prism-agent:** Add prism agent record processing pipeline parameters ([#626](https://github.com/input-output-hk/atala-prism-building-blocks/issues/626)) ([434bdac](https://github.com/input-output-hk/atala-prism-building-blocks/commit/434bdacfc10b854b77bde0c8c7add613d8ee9025))

# [1.10.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.9.2...prism-agent-v1.10.0) (2023-08-16)


### Bug Fixes

* **prism-agent:** fix OAS on empty DID resolution representation ([#616](https://github.com/input-output-hk/atala-prism-building-blocks/issues/616)) ([216ff3a](https://github.com/input-output-hk/atala-prism-building-blocks/commit/216ff3a2ef75d824d0a6285218be01636a595a82))


### Features

* **agent:** improve OAS spec and refactor DidCommHttpServer code ([#615](https://github.com/input-output-hk/atala-prism-building-blocks/issues/615)) ([301fbab](https://github.com/input-output-hk/atala-prism-building-blocks/commit/301fbabac6c743130c46572056d9b8848a166be1))
* **prism-agent:** Metrics for connection flow job ([#611](https://github.com/input-output-hk/atala-prism-building-blocks/issues/611)) ([695d661](https://github.com/input-output-hk/atala-prism-building-blocks/commit/695d66173b40b3ee9f87c3b950b54bdeff8f02d2))
* update anoncreds demo after the new getJson methods ([#584](https://github.com/input-output-hk/atala-prism-building-blocks/issues/584)) ([d8258ee](https://github.com/input-output-hk/atala-prism-building-blocks/commit/d8258ee5d29b94ac863f9dbf5c8eaadd66fd636e))


### Performance Improvements

* support for credential and present-proof flows with thid ([#609](https://github.com/input-output-hk/atala-prism-building-blocks/issues/609)) ([9cef8c0](https://github.com/input-output-hk/atala-prism-building-blocks/commit/9cef8c03cf0a3e5601ec36b1f008dea2a738a415))

## [1.9.2](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.9.1...prism-agent-v1.9.2) (2023-07-27)


### Bug Fixes

* consumer variable nesting correction ([#606](https://github.com/input-output-hk/atala-prism-building-blocks/issues/606)) ([40a0578](https://github.com/input-output-hk/atala-prism-building-blocks/commit/40a0578274d33873c5189d01715244b2b34c0fea))

## [1.9.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.9.0...prism-agent-v1.9.1) (2023-07-25)


### Bug Fixes

* include helm Chart.yaml in git commit for release process ([#604](https://github.com/input-output-hk/atala-prism-building-blocks/issues/604)) ([d0372f1](https://github.com/input-output-hk/atala-prism-building-blocks/commit/d0372f19e74ade5627a41038b07010321d5ef600))

# [1.9.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.8.0...prism-agent-v1.9.0) (2023-07-25)


### Features

* add helm-chart for agent  ([#603](https://github.com/input-output-hk/atala-prism-building-blocks/issues/603)) ([63f38d4](https://github.com/input-output-hk/atala-prism-building-blocks/commit/63f38d47f4645bf6172320da5c3413c748c03729))

# [1.8.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.7.0...prism-agent-v1.8.0) (2023-07-20)


### Bug Fixes

* **castor:** align DID document translation logic ([#595](https://github.com/input-output-hk/atala-prism-building-blocks/issues/595)) ([bb1f112](https://github.com/input-output-hk/atala-prism-building-blocks/commit/bb1f1121975c3bc8288b1d4577efd3922e5adce7))
* **prism-agent:** add did-method path segment in HD key derivation ([#596](https://github.com/input-output-hk/atala-prism-building-blocks/issues/596)) ([a1e457a](https://github.com/input-output-hk/atala-prism-building-blocks/commit/a1e457a8d6337e8c941b58c802f9516fe6718396))


### Features

* ATL-4888 Anoncred schema type ([#590](https://github.com/input-output-hk/atala-prism-building-blocks/issues/590)) ([a57deef](https://github.com/input-output-hk/atala-prism-building-blocks/commit/a57deef485fea5181e8617a30ab70ca26c409b42))

# [1.7.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.6.0...prism-agent-v1.7.0) (2023-07-10)


### Bug Fixes

* Adding Apollo ADR ([#573](https://github.com/input-output-hk/atala-prism-building-blocks/issues/573)) ([e036bc8](https://github.com/input-output-hk/atala-prism-building-blocks/commit/e036bc84446c8b7eb008f536def2adaca08e071f))
* **castor:** fix DID parser that failing to parse some DIDs ([#581](https://github.com/input-output-hk/atala-prism-building-blocks/issues/581)) ([24b2300](https://github.com/input-output-hk/atala-prism-building-blocks/commit/24b230023ad2812dc13aa7229163ead5eb56183d))
* **pollux:** add pagination at db level for getCredentialRecords ([#586](https://github.com/input-output-hk/atala-prism-building-blocks/issues/586)) ([c0db5c8](https://github.com/input-output-hk/atala-prism-building-blocks/commit/c0db5c8a2a4fee7568fb5aa43f81a2faba6936a2))


### Features

* **prism-agent:** add http metrics ([#585](https://github.com/input-output-hk/atala-prism-building-blocks/issues/585)) ([f62d7f5](https://github.com/input-output-hk/atala-prism-building-blocks/commit/f62d7f5459f12f93224b0eb9b05caf605f54be2c))
* **prism-agent:** align DID document service handling with the spec ([#582](https://github.com/input-output-hk/atala-prism-building-blocks/issues/582)) ([c9e69f6](https://github.com/input-output-hk/atala-prism-building-blocks/commit/c9e69f602ef5e78848ad6d652f0ba7d4d4d2db2d))
* **prism-agent:** expose connect/issue/presentation records 'thid' and add it to REST API queries ([#583](https://github.com/input-output-hk/atala-prism-building-blocks/issues/583)) ([9a97c7a](https://github.com/input-output-hk/atala-prism-building-blocks/commit/9a97c7a6e5815fd80eba0f98042a49356fc1f61c))
* **prism-agent:** simple event mechanism using webhook ([#575](https://github.com/input-output-hk/atala-prism-building-blocks/issues/575)) ([42cf8c9](https://github.com/input-output-hk/atala-prism-building-blocks/commit/42cf8c9b47b2ac2d17e6d00b0901806e0f0e2e1d)), closes [#1](https://github.com/input-output-hk/atala-prism-building-blocks/issues/1)


### Performance Improvements

* add k6 connection flow running in CI ([#572](https://github.com/input-output-hk/atala-prism-building-blocks/issues/572)) ([601f934](https://github.com/input-output-hk/atala-prism-building-blocks/commit/601f934062537c8080657b6268299f18d8201ec2))

# [1.6.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.5.1...prism-agent-v1.6.0) (2023-06-28)


### Bug Fixes

* **prism-agent:** decouple secret storage backend from agent ([#570](https://github.com/input-output-hk/atala-prism-building-blocks/issues/570)) ([6a5f9ce](https://github.com/input-output-hk/atala-prism-building-blocks/commit/6a5f9cef337848dadd8a54b54948db9e7edfe8ad))
* **prism-agent:** fix concurrent requests breaking DID index counter ([#571](https://github.com/input-output-hk/atala-prism-building-blocks/issues/571)) ([e8411dd](https://github.com/input-output-hk/atala-prism-building-blocks/commit/e8411ddb588e9dc81f2437cfbdfdcd1be42f99d1))
* **prism-agent:** use correct pairwise DIDs in presentation flow ([#568](https://github.com/input-output-hk/atala-prism-building-blocks/issues/568)) ([ede234b](https://github.com/input-output-hk/atala-prism-building-blocks/commit/ede234bbdcb64cb48da182b374288b549b8cf8aa))


### Features

* new Anoncreds Demo ([#562](https://github.com/input-output-hk/atala-prism-building-blocks/issues/562)) ([a9a8290](https://github.com/input-output-hk/atala-prism-building-blocks/commit/a9a8290c73fb3044c2091311c199d1e532af03f0))

## [1.5.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.5.0...prism-agent-v1.5.1) (2023-06-22)


### Bug Fixes

* log seed error before effect fail ([#557](https://github.com/input-output-hk/atala-prism-building-blocks/issues/557)) ([c3a5d8e](https://github.com/input-output-hk/atala-prism-building-blocks/commit/c3a5d8eb9e62675053f9b7fc80ee18d7a62f857c))

# [1.5.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.4.0...prism-agent-v1.5.0) (2023-06-16)


### Features

* **prism-agent:** integrate credential schema into VC issue flow ([#541](https://github.com/input-output-hk/atala-prism-building-blocks/issues/541)) ([ab88736](https://github.com/input-output-hk/atala-prism-building-blocks/commit/ab88736dc9d6dfec3d39f6a58619eb115f520bf8))
* **prism-agent:** integrate DID secret storage with Vault ([#543](https://github.com/input-output-hk/atala-prism-building-blocks/issues/543)) ([ee43feb](https://github.com/input-output-hk/atala-prism-building-blocks/commit/ee43febacddb06210065c3f812beb8c948d5c369))

# [1.4.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.3.0...prism-agent-v1.4.0) (2023-06-01)


### Bug Fixes

* **prism-agent:** infinite loop in proof presentation execution ([#540](https://github.com/input-output-hk/atala-prism-building-blocks/issues/540)) ([6a26bb7](https://github.com/input-output-hk/atala-prism-building-blocks/commit/6a26bb78d256bdcd09918cb1e8ee5bfd5cf0dacc))


### Features

* **prism-agent:** add support for hierarchical deterministic key with seed ([#534](https://github.com/input-output-hk/atala-prism-building-blocks/issues/534)) ([6129baf](https://github.com/input-output-hk/atala-prism-building-blocks/commit/6129baf1210b68decc4f264bd4a64b4009719956))

# [1.3.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.2.0...prism-agent-v1.3.0) (2023-05-23)


### Features

* restore JVM metrics endpoint capability ([#527](https://github.com/input-output-hk/atala-prism-building-blocks/issues/527)) ([7d603f0](https://github.com/input-output-hk/atala-prism-building-blocks/commit/7d603f09abd6042368ada6afa3685332342d6860))

# [1.2.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.1.0...prism-agent-v1.2.0) (2023-05-17)


### Bug Fixes

* **prism-agent:** refactor crypto abstraction in the walletAPI ([#522](https://github.com/input-output-hk/atala-prism-building-blocks/issues/522)) ([e36c634](https://github.com/input-output-hk/atala-prism-building-blocks/commit/e36c63424ed2e28fc360c6a6a5d557938d4ec01a))


### Features

* migrate issue endpoint to tapir ([#516](https://github.com/input-output-hk/atala-prism-building-blocks/issues/516)) ([9b1558f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/9b1558f50003ba1c79ec2cdd9888f2e99f0534d8))
* **prism-agent:** fix infinite reprocessing of records in error ([#528](https://github.com/input-output-hk/atala-prism-building-blocks/issues/528)) ([904a2dc](https://github.com/input-output-hk/atala-prism-building-blocks/commit/904a2dcb09d2e907e284479c652c5f389fd0dec9))
* **prism-agent:** migrate present-proof endpoints to Tapir ([#525](https://github.com/input-output-hk/atala-prism-building-blocks/issues/525)) ([cb01657](https://github.com/input-output-hk/atala-prism-building-blocks/commit/cb016570b6d0a1b0de98928d6daa1cbf055d26b4))

# [1.1.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v1.0.0...prism-agent-v1.1.0) (2023-05-05)


### Bug Fixes

* **pollux:** ATL-3971 Fix all Deprecation Warning ([#506](https://github.com/input-output-hk/atala-prism-building-blocks/issues/506)) ([e5225b7](https://github.com/input-output-hk/atala-prism-building-blocks/commit/e5225b7101bf3572a85a6f0cf8ed05e93410f551))


### Features

* add multi-arch amd64 and arm64 support for agent docker ([#512](https://github.com/input-output-hk/atala-prism-building-blocks/issues/512)) ([dc2608c](https://github.com/input-output-hk/atala-prism-building-blocks/commit/dc2608c12e062a6af5d3fcf1077956281a2f0828))
* expose API for default did:peer of prism agent ([#509](https://github.com/input-output-hk/atala-prism-building-blocks/issues/509)) ([b128292](https://github.com/input-output-hk/atala-prism-building-blocks/commit/b128292031c547938614b21fb4cd0a377310c19e))
* **prism-agent:** migrate DID endpoint to tapir ([#511](https://github.com/input-output-hk/atala-prism-building-blocks/issues/511)) ([9d587ff](https://github.com/input-output-hk/atala-prism-building-blocks/commit/9d587ffc6e44da9dacb0af76e922030828831805))
* **prism-agent:** migrate did-registrar endpoint to tapir ([#517](https://github.com/input-output-hk/atala-prism-building-blocks/issues/517)) ([88eeefd](https://github.com/input-output-hk/atala-prism-building-blocks/commit/88eeefdad81e05197ea0b6c2bf449c4c2960e023))

# [1.0.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.60.2...prism-agent-v1.0.0) (2023-04-10)


### Features

* add prism-agent-* prefix to the tag ([#505](https://github.com/input-output-hk/atala-prism-building-blocks/issues/505)) ([6087f2d](https://github.com/input-output-hk/atala-prism-building-blocks/commit/6087f2dcc77179a4bb4702e60a9669c6329ba55c))
* **castor:** add support for context in DID document & operation ([#489](https://github.com/input-output-hk/atala-prism-building-blocks/issues/489)) ([8384fe3](https://github.com/input-output-hk/atala-prism-building-blocks/commit/8384fe39be38f24e6b821851781c6b465e8e4bfa))
* **prism-agent:** migrate connect endpoints to Tapir ([#493](https://github.com/input-output-hk/atala-prism-building-blocks/issues/493)) ([876dd9e](https://github.com/input-output-hk/atala-prism-building-blocks/commit/876dd9ed4b89f7c2cf779d47bb89b8a0358743db))
* **prism-node:** add context to protobuf definition ([#487](https://github.com/input-output-hk/atala-prism-building-blocks/issues/487)) ([e426a82](https://github.com/input-output-hk/atala-prism-building-blocks/commit/e426a82b4f593204f1dc69c2b65c7362e8707ec6))
* Reply to Trust Pings ([#496](https://github.com/input-output-hk/atala-prism-building-blocks/issues/496)) ([b07da78](https://github.com/input-output-hk/atala-prism-building-blocks/commit/b07da78d3ee927c7ddfbd311d442a687b0b901a4))


### BREAKING CHANGES

* incrementing major version for the first stable release

# [prism-agent-v0.60.2](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.60.1...prism-agent-v0.60.2) (2023-03-29)


### Bug Fixes

* **prism-agent:** Alight the error responses according to RFC7807. ATL-3962 ([#480](https://github.com/input-output-hk/atala-prism-building-blocks/issues/480)) ([64b0a2a](https://github.com/input-output-hk/atala-prism-building-blocks/commit/64b0a2a04599c30adaf64e8411e1ec95305846cd))

# [prism-agent-v0.60.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.60.0...prism-agent-v0.60.1) (2023-03-28)


### Bug Fixes

* use shared postgres container for tests ([#486](https://github.com/input-output-hk/atala-prism-building-blocks/issues/486)) ([1d6aada](https://github.com/input-output-hk/atala-prism-building-blocks/commit/1d6aada72fedf6420133451214ca27965cff245f))

# [prism-agent-v0.60.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.59.1...prism-agent-v0.60.0) (2023-03-28)


### Features

* **prism-agent:** insert bouncy-castle security as 2nd provider globally in agent entry point ([#477](https://github.com/input-output-hk/atala-prism-building-blocks/issues/477)) ([44f06cc](https://github.com/input-output-hk/atala-prism-building-blocks/commit/44f06cc191d10e8f590ba56ced75c78e73089b7b))

# [prism-agent-v0.59.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.59.0...prism-agent-v0.59.1) (2023-03-23)


### Bug Fixes

* **prism-agent:** fix incorrect long-form parsing behavior on resolution endpoint ([#475](https://github.com/input-output-hk/atala-prism-building-blocks/issues/475)) ([af356d6](https://github.com/input-output-hk/atala-prism-building-blocks/commit/af356d66763197f118aee8f7d1ac7d82be05a46e))

# [prism-agent-v0.59.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.58.0...prism-agent-v0.59.0) (2023-03-22)


### Features

* **prism-agent:** add universal-resolver compatible endpoint ([#455](https://github.com/input-output-hk/atala-prism-building-blocks/issues/455)) ([1cbb729](https://github.com/input-output-hk/atala-prism-building-blocks/commit/1cbb729c05da51d98163a9c99d035f2814cc9fb1))

# [prism-agent-v0.58.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.57.0...prism-agent-v0.58.0) (2023-03-22)


### Features

* **prism-agent:** Update the connect pollux and mercury to fix to use nimbus-jose-jwt 10.0.0-preview ([#467](https://github.com/input-output-hk/atala-prism-building-blocks/issues/467)) ([948f2fc](https://github.com/input-output-hk/atala-prism-building-blocks/commit/948f2fc26a7b965c47e7907cf4d4eb14bb9e640f))

# [prism-agent-v0.57.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.56.0...prism-agent-v0.57.0) (2023-03-21)


### Features

* **prism-agent:** Update pollux lib to 0.43.0 includes the nimbus-jo… ([#460](https://github.com/input-output-hk/atala-prism-building-blocks/issues/460)) ([adb7000](https://github.com/input-output-hk/atala-prism-building-blocks/commit/adb7000348a29a83ca59f0d945b1327795dd3b42))

# [prism-agent-v0.56.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.55.0...prism-agent-v0.56.0) (2023-03-17)


### Features

* **prism-agent:** allow published and unpublished DIDs in the issuingDID field ([#454](https://github.com/input-output-hk/atala-prism-building-blocks/issues/454)) ([ec107ad](https://github.com/input-output-hk/atala-prism-building-blocks/commit/ec107ad42a66da3703ce60b1fe7697217683c89c))

# [prism-agent-v0.55.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.54.0...prism-agent-v0.55.0) (2023-03-16)


### Features

* **prism-agent:** VerificationOptions are configurable for PrismAgent ([#449](https://github.com/input-output-hk/atala-prism-building-blocks/issues/449)) ([ee93880](https://github.com/input-output-hk/atala-prism-building-blocks/commit/ee93880dff6bd8a1fa098c3e1afd29939a6216dd))

# [prism-agent-v0.54.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.53.0...prism-agent-v0.54.0) (2023-03-16)


### Features

* **prism-agent:** update pollux to 0.41.0; ([#447](https://github.com/input-output-hk/atala-prism-building-blocks/issues/447)) ([f52c5e0](https://github.com/input-output-hk/atala-prism-building-blocks/commit/f52c5e007821e9df1b56e882d443bc31755c1ee6))

# [prism-agent-v0.53.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.52.0...prism-agent-v0.53.0) (2023-03-14)


### Features

* **prism-agent:** Add OAS specification to the schema registry endpoint. ATL-3164 ([#438](https://github.com/input-output-hk/atala-prism-building-blocks/issues/438)) ([91902ce](https://github.com/input-output-hk/atala-prism-building-blocks/commit/91902ce3840227ed4d683b55060d9603320ff9fd))

# [prism-agent-v0.52.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.51.0...prism-agent-v0.52.0) (2023-03-13)


### Features

* **prism-agent:** Add filter by thid to OpenAPI when fetching records ([#436](https://github.com/input-output-hk/atala-prism-building-blocks/issues/436)) ([af01359](https://github.com/input-output-hk/atala-prism-building-blocks/commit/af0135904d4961d12e109004e7b001b154b1493e))

# [prism-agent-v0.51.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.50.0...prism-agent-v0.51.0) (2023-03-10)


### Features

* **prism-agent:** move subjectId field from issuer to holder ([#435](https://github.com/input-output-hk/atala-prism-building-blocks/issues/435)) ([d7813ba](https://github.com/input-output-hk/atala-prism-building-blocks/commit/d7813ba9b3d5a1c422d3d7e7fb57af4dabcb0d54))

# [prism-agent-v0.50.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.49.0...prism-agent-v0.50.0) (2023-03-10)


### Features

* **prism-agent:** Added connectionId to the request Presentation and deleted the readme files as they are moved here https://github.com/input-output-hk/atala-prism-building-blocks/tree/main/docs/docusaurus ([#432](https://github.com/input-output-hk/atala-prism-building-blocks/issues/432)) ([301022a](https://github.com/input-output-hk/atala-prism-building-blocks/commit/301022a372e40b126b389548ef0c9acb4f0382f0))

# [prism-agent-v0.49.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.48.4...prism-agent-v0.49.0) (2023-03-09)


### Features

* **prism-agent:** CredentialSchema DAL, model, service and repositor… ([#425](https://github.com/input-output-hk/atala-prism-building-blocks/issues/425)) ([32f9e83](https://github.com/input-output-hk/atala-prism-building-blocks/commit/32f9e832a7789a971a4506a8971b6ba8b06daabe))

# [prism-agent-v0.48.4](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.48.3...prism-agent-v0.48.4) (2023-03-09)


### Bug Fixes

* **prism-agent:** Fix for ATL-3624 ([#430](https://github.com/input-output-hk/atala-prism-building-blocks/issues/430)) ([02fe4d8](https://github.com/input-output-hk/atala-prism-building-blocks/commit/02fe4d8cab14eb2b54d13dc726573d07bf77b76a))

# [prism-agent-v0.48.3](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.48.2...prism-agent-v0.48.3) (2023-03-03)


### Bug Fixes

* **prism-agent:** update pollux to 0.35.2 ([#419](https://github.com/input-output-hk/atala-prism-building-blocks/issues/419)) ([63cd430](https://github.com/input-output-hk/atala-prism-building-blocks/commit/63cd4305cfe10b6be5d57d1d2988536eefde35f0))

# [prism-agent-v0.48.2](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.48.1...prism-agent-v0.48.2) (2023-03-03)


### Bug Fixes

* **prism-agent:** avoid race condition when update or deactivate DID ([#415](https://github.com/input-output-hk/atala-prism-building-blocks/issues/415)) ([bf03674](https://github.com/input-output-hk/atala-prism-building-blocks/commit/bf03674769f0b6163de13f4002198902fdd413e9))

# [prism-agent-v0.48.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.48.0...prism-agent-v0.48.1) (2023-03-02)


### Bug Fixes

* **prism-agent:** update pollux to 0.35.1 ([#414](https://github.com/input-output-hk/atala-prism-building-blocks/issues/414)) ([20770c8](https://github.com/input-output-hk/atala-prism-building-blocks/commit/20770c84a67ca4105e964e97de7aeddbbcab5941))

# [prism-agent-v0.48.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.47.1...prism-agent-v0.48.0) (2023-03-02)


### Features

* **prism-agent:** Added new state PresentationVerificationFailed ([#412](https://github.com/input-output-hk/atala-prism-building-blocks/issues/412)) ([55569ed](https://github.com/input-output-hk/atala-prism-building-blocks/commit/55569edbae03a034b84548878fe3ab19252b3bb3))

# [prism-agent-v0.47.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.47.0...prism-agent-v0.47.1) (2023-03-02)


### Bug Fixes

* **prism-agent:** add consistency to documentation of OAS on DID endpoints ([#408](https://github.com/input-output-hk/atala-prism-building-blocks/issues/408)) ([dd04c3f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/dd04c3fd14c76c02a7cfbb26ca52141590c48371))

# [prism-agent-v0.47.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.46.0...prism-agent-v0.47.0) (2023-03-02)


### Features

* **prism-agent:** update pollux from 0.33.0 to 0.35.0 ([#410](https://github.com/input-output-hk/atala-prism-building-blocks/issues/410)) ([59afe8c](https://github.com/input-output-hk/atala-prism-building-blocks/commit/59afe8cd2acea17fd201378066b89f0bfcb8e98a))

# [prism-agent-v0.46.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.45.1...prism-agent-v0.46.0) (2023-03-02)


### Features

* **prism-agent:** update libs: pollux 0.33.0; connect 0.11.0; mercury 0.20.0 ([#403](https://github.com/input-output-hk/atala-prism-building-blocks/issues/403)) ([d724a02](https://github.com/input-output-hk/atala-prism-building-blocks/commit/d724a02a7b5551bb8a49ddd702a6b14c42f53a81))

# [prism-agent-v0.45.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.45.0...prism-agent-v0.45.1) (2023-02-28)


### Bug Fixes

* **prism-agent:** add uri normalization on UpdateService patch ([#401](https://github.com/input-output-hk/atala-prism-building-blocks/issues/401)) ([6a98f70](https://github.com/input-output-hk/atala-prism-building-blocks/commit/6a98f7015069dba781d03584ae97a40681c5a5a9))

# [prism-agent-v0.45.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.44.0...prism-agent-v0.45.0) (2023-02-27)


### Features

* **prism-agent:** add JVM metrics endpoint, add health/version endpoint ([#390](https://github.com/input-output-hk/atala-prism-building-blocks/issues/390)) ([6d3e5a0](https://github.com/input-output-hk/atala-prism-building-blocks/commit/6d3e5a038b75250c3813a5454f1547247b5e5d13))

# [prism-agent-v0.44.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.43.0...prism-agent-v0.44.0) (2023-02-27)


### Features

* **prism-agent:** add pagination to did-registrar list DID endpoint ([#394](https://github.com/input-output-hk/atala-prism-building-blocks/issues/394)) ([a21e388](https://github.com/input-output-hk/atala-prism-building-blocks/commit/a21e38880a5d49e0616e30b9dd9b92dc01980256))
* **prsim-agent:** pollux version updated ([#392](https://github.com/input-output-hk/atala-prism-building-blocks/issues/392)) ([409b673](https://github.com/input-output-hk/atala-prism-building-blocks/commit/409b673e954b913d7bebf31cdf0c2e8dcee3ce03))

# [prism-agent-v0.43.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.42.0...prism-agent-v0.43.0) (2023-02-22)


### Features

* **prism-agent:** fix DID service URI class and improve validation error response message ([#389](https://github.com/input-output-hk/atala-prism-building-blocks/issues/389)) ([3d08996](https://github.com/input-output-hk/atala-prism-building-blocks/commit/3d08996b8e92c427b317c9fac50e2d8bce85cb78))

# [prism-agent-v0.42.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.41.0...prism-agent-v0.42.0) (2023-02-21)


### Features

* **prism-agent:** set OAS version to 0.41.0 ([#386](https://github.com/input-output-hk/atala-prism-building-blocks/issues/386)) ([198643e](https://github.com/input-output-hk/atala-prism-building-blocks/commit/198643e3856c51dcd1e68047f9269cdd71238923))

# [prism-agent-v0.41.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.40.1...prism-agent-v0.41.0) (2023-02-20)


### Features

* **prism-agent:** do not create new Prism DID when proof presentation is generated ([#378](https://github.com/input-output-hk/atala-prism-building-blocks/issues/378)) ([1aa856f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/1aa856f4281c23f6fd2f12b8e250ed1be285d49e))

# [prism-agent-v0.40.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.40.0...prism-agent-v0.40.1) (2023-02-17)


### Bug Fixes

* **prism-agent:** Update Mercury Connect Pollux ([#382](https://github.com/input-output-hk/atala-prism-building-blocks/issues/382)) ([b7f02ac](https://github.com/input-output-hk/atala-prism-building-blocks/commit/b7f02ac909098159fbc0cb4187384b0ba007524a))

# [prism-agent-v0.40.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.39.0...prism-agent-v0.40.0) (2023-02-16)


### Features

* **prism-agent:** ATL-3554 clean-up OAS and remove unused endpoints and definitions ([#376](https://github.com/input-output-hk/atala-prism-building-blocks/issues/376)) ([146cd52](https://github.com/input-output-hk/atala-prism-building-blocks/commit/146cd52062c712de30c9862c32cba73ebe213a89))

# [prism-agent-v0.39.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.38.0...prism-agent-v0.39.0) (2023-02-15)


### Features

* **prism-agent:** issue credential to Prism DID holder by Prism DID issuer ([#373](https://github.com/input-output-hk/atala-prism-building-blocks/issues/373)) ([1c1a171](https://github.com/input-output-hk/atala-prism-building-blocks/commit/1c1a171d1ba983a9c93644ded4feafe0ed6e5294))

# [prism-agent-v0.38.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.37.0...prism-agent-v0.38.0) (2023-02-13)


### Features

* **prism-agent:** integrate VerificationPolicy DAL into the agent, update OAS and implement REST API for verification policies ([#369](https://github.com/input-output-hk/atala-prism-building-blocks/issues/369)) ([142ff55](https://github.com/input-output-hk/atala-prism-building-blocks/commit/142ff550a278e7f28d97c539e529fea3cc92c178))

# [prism-agent-v0.37.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.36.0...prism-agent-v0.37.0) (2023-02-09)


### Features

* **prism-agent:** Added challenge and domain and to protect agains r… ([#364](https://github.com/input-output-hk/atala-prism-building-blocks/issues/364)) ([4f0b261](https://github.com/input-output-hk/atala-prism-building-blocks/commit/4f0b261b3545c8681eccd8b38c7fa028ee840c50))

# [prism-agent-v0.36.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.35.0...prism-agent-v0.36.0) (2023-02-07)


### Features

* **prism-agent:** ATL-3349 connection state issue and perf improvements ([#359](https://github.com/input-output-hk/atala-prism-building-blocks/issues/359)) ([c77f160](https://github.com/input-output-hk/atala-prism-building-blocks/commit/c77f160043262662ce7d5d9c6f75cd893bcab68d))

# [prism-agent-v0.35.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.34.0...prism-agent-v0.35.0) (2023-02-07)


### Features

* **prism-agent:** Upgrade libs ([#344](https://github.com/input-output-hk/atala-prism-building-blocks/issues/344)) ([64a7857](https://github.com/input-output-hk/atala-prism-building-blocks/commit/64a7857ecc0acc6d940e7f2fcce1c68c8163562c))

# [prism-agent-v0.34.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.33.0...prism-agent-v0.34.0) (2023-02-06)


### Features

* **prism-agent:** upgrade castor and pollux and align OAS to the DID spec ([#342](https://github.com/input-output-hk/atala-prism-building-blocks/issues/342)) ([b8643a8](https://github.com/input-output-hk/atala-prism-building-blocks/commit/b8643a81710300082bfaceb08f906d364869d405))

# [prism-agent-v0.33.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.32.0...prism-agent-v0.33.0) (2023-02-02)


### Features

* **prism-agent:** Upgrade mercury connect pollux ([#340](https://github.com/input-output-hk/atala-prism-building-blocks/issues/340)) ([3758232](https://github.com/input-output-hk/atala-prism-building-blocks/commit/3758232448ae66fbc444c487f32917a106645473))

# [prism-agent-v0.32.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.31.1...prism-agent-v0.32.0) (2023-01-27)


### Features

* **prism-agent:** add deactivate DID endpoint ([#326](https://github.com/input-output-hk/atala-prism-building-blocks/issues/326)) ([29a804f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/29a804fce71d2c500cec9ab8db21de84f9016c95))

# [prism-agent-v0.31.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.31.0...prism-agent-v0.31.1) (2023-01-26)


### Bug Fixes

* **prism-agent:** remove deprecated did-auth endpoints ([#324](https://github.com/input-output-hk/atala-prism-building-blocks/issues/324)) ([a934cd4](https://github.com/input-output-hk/atala-prism-building-blocks/commit/a934cd4ac48f4ba4724681eeff92f4c67c009940))

# [prism-agent-v0.31.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.30.0...prism-agent-v0.31.0) (2023-01-25)


### Features

* **prism-agent:** remove connection deletion enpoint from OAS spec ([#323](https://github.com/input-output-hk/atala-prism-building-blocks/issues/323)) ([cb17acf](https://github.com/input-output-hk/atala-prism-building-blocks/commit/cb17acf782500c0885c077570392423deee9a2f2))

# [prism-agent-v0.30.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.29.0...prism-agent-v0.30.0) (2023-01-23)


### Features

* **prism-agent:** enable update DID operation on prism-agent ([#307](https://github.com/input-output-hk/atala-prism-building-blocks/issues/307)) ([a57365f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/a57365f8c063958e7a41e01fde71653843dafe24))

# [prism-agent-v0.29.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.28.0...prism-agent-v0.29.0) (2023-01-20)


### Features

* **prism-agent:** implement DAL for the credential schema. ATL-1334 ([3d0c642](https://github.com/input-output-hk/atala-prism-building-blocks/commit/3d0c6426cc7fddbce41de16a1c85f4242e046c6a))

# [prism-agent-v0.28.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.27.0...prism-agent-v0.28.0) (2023-01-06)


### Features

* **prismagent:** prism did creation and use  updated protocol state ([#291](https://github.com/input-output-hk/atala-prism-building-blocks/issues/291)) ([cc7c533](https://github.com/input-output-hk/atala-prism-building-blocks/commit/cc7c53380faba9afe36006b8bf68621862c9b902))

# [prism-agent-v0.27.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.26.0...prism-agent-v0.27.0) (2022-12-22)


### Features

* **prism-agent:** implement JDBC did nonsecret storage ([#284](https://github.com/input-output-hk/atala-prism-building-blocks/issues/284)) ([7e116a3](https://github.com/input-output-hk/atala-prism-building-blocks/commit/7e116a38b44408b3d0c875bc79415ea6d0579ffa))

# [prism-agent-v0.26.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.25.0...prism-agent-v0.26.0) (2022-12-20)


### Features

* **prism-agent:** add 'GET /present-proof/presentations/{id}' endpoint ([#282](https://github.com/input-output-hk/atala-prism-building-blocks/issues/282)) ([030a257](https://github.com/input-output-hk/atala-prism-building-blocks/commit/030a257efa89f5fc473549dc17657160fda0b26b))

# [prism-agent-v0.25.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.24.0...prism-agent-v0.25.0) (2022-12-20)


### Features

* **prism-agnet:** Verifiable Credential ([#281](https://github.com/input-output-hk/atala-prism-building-blocks/issues/281)) ([ae74e20](https://github.com/input-output-hk/atala-prism-building-blocks/commit/ae74e20080cfcac7263c7540c8ed5b82070428e1))

# [prism-agent-v0.24.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.23.0...prism-agent-v0.24.0) (2022-12-20)


### Features

* **prism-agent:** allow connection reuse when creating credential offer ([#276](https://github.com/input-output-hk/atala-prism-building-blocks/issues/276)) ([eff3918](https://github.com/input-output-hk/atala-prism-building-blocks/commit/eff3918c98ca3f2edc708688edd2ece63f7f5aa9))

# [prism-agent-v0.23.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.22.0...prism-agent-v0.23.0) (2022-12-19)


### Features

* **prism-agent:** bump mercury, pollux and connect versions ([#273](https://github.com/input-output-hk/atala-prism-building-blocks/issues/273)) ([ce4758b](https://github.com/input-output-hk/atala-prism-building-blocks/commit/ce4758bf0e1a34b0953c05eea63ae06a389fc532))

# [prism-agent-v0.22.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.21.0...prism-agent-v0.22.0) (2022-12-16)


### Features

* **prism-agent:** use pairwise DID in proof presentation flow ([#260](https://github.com/input-output-hk/atala-prism-building-blocks/issues/260)) ([bb04ca4](https://github.com/input-output-hk/atala-prism-building-blocks/commit/bb04ca4c4d2a2c18e8be7f2932c0546888ffce05))

# [prism-agent-v0.21.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.20.0...prism-agent-v0.21.0) (2022-12-15)


### Features

* **prism-agent:** integrate latest pollux version ([#259](https://github.com/input-output-hk/atala-prism-building-blocks/issues/259)) ([d199f0d](https://github.com/input-output-hk/atala-prism-building-blocks/commit/d199f0db0c40683bfafdeec4726b517172f8a769))

# [prism-agent-v0.20.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.19.0...prism-agent-v0.20.0) (2022-12-14)


### Features

* **prism-agent:** fix DB connection pool duplication issue by providing ZLayer globally ([#256](https://github.com/input-output-hk/atala-prism-building-blocks/issues/256)) ([4424de1](https://github.com/input-output-hk/atala-prism-building-blocks/commit/4424de14917988cc6f8e406624bf7cf20db27455))

# [prism-agent-v0.19.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.18.0...prism-agent-v0.19.0) (2022-12-14)


### Features

* **prism-agent:** Use pairwise PeerDIDs in connect and issue ([#253](https://github.com/input-output-hk/atala-prism-building-blocks/issues/253)) ([01519ff](https://github.com/input-output-hk/atala-prism-building-blocks/commit/01519ffc7d608c3d17ffa06242bf7324681c31d1))

# [prism-agent-v0.18.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.17.1...prism-agent-v0.18.0) (2022-12-14)


### Features

* **prism-agent:** Update mercury to 0.12.0 & pollux to 0.10.0 & connect to 0.4.0 ([#248](https://github.com/input-output-hk/atala-prism-building-blocks/issues/248)) ([33ed7ba](https://github.com/input-output-hk/atala-prism-building-blocks/commit/33ed7ba56e95bcecc75f43b8fbce77b685530588))

# [prism-agent-v0.17.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.17.0...prism-agent-v0.17.1) (2022-12-13)


### Bug Fixes

* **prism-agent:** switch datetime format to offsetdatetime. ATL-2723 ([#243](https://github.com/input-output-hk/atala-prism-building-blocks/issues/243)) ([6903afa](https://github.com/input-output-hk/atala-prism-building-blocks/commit/6903afa8d3ba226f02b1dce7665cf5adf3fc09e6))

# [prism-agent-v0.17.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.16.1...prism-agent-v0.17.0) (2022-12-12)


### Features

* **prism-agent:** Integrate Verification Flow - ATL-2117 ([#147](https://github.com/input-output-hk/atala-prism-building-blocks/issues/147)) ([cabda08](https://github.com/input-output-hk/atala-prism-building-blocks/commit/cabda08f215d911772440853ec153a22ac6adaad))

# [prism-agent-v0.16.1](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.16.0...prism-agent-v0.16.1) (2022-12-12)


### Bug Fixes

* **prism-agent:** didcomm endpoint now exposed in docker file and with correct path ([#241](https://github.com/input-output-hk/atala-prism-building-blocks/issues/241)) ([405f367](https://github.com/input-output-hk/atala-prism-building-blocks/commit/405f3672d48dd47559f10f89db5cd7051a0c9eeb))

# [prism-agent-v0.16.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.15.0...prism-agent-v0.16.0) (2022-12-12)


### Features

* **prism-agent:** upgrade castor version with DID serviceEndpoint ([#229](https://github.com/input-output-hk/atala-prism-building-blocks/issues/229)) ([0ba3b89](https://github.com/input-output-hk/atala-prism-building-blocks/commit/0ba3b892b25e66c081c931b99c43e60d3c51af2a))

# [prism-agent-v0.15.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.14.0...prism-agent-v0.15.0) (2022-12-09)


### Features

* **prism-agent:** bump dependencies version ([#228](https://github.com/input-output-hk/atala-prism-building-blocks/issues/228)) ([8a6bad5](https://github.com/input-output-hk/atala-prism-building-blocks/commit/8a6bad539334775d25ef9b5cf0eb042832ecd272))

# [prism-agent-v0.14.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.13.0...prism-agent-v0.14.0) (2022-12-07)


### Features

* **infra:** switch to single instance of postgres for running locally ([#203](https://github.com/input-output-hk/atala-prism-building-blocks/issues/203)) ([32e33f1](https://github.com/input-output-hk/atala-prism-building-blocks/commit/32e33f109f834386cfda5168a00e8407ca136e2e))

# [prism-agent-v0.13.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.12.0...prism-agent-v0.13.0) (2022-12-07)


### Features

* **prism-agent:** remove unused / deprecated DID endpoints ([#213](https://github.com/input-output-hk/atala-prism-building-blocks/issues/213)) ([0308b4f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/0308b4fdc6da78643354391234a6139d79cd8e90))

# [prism-agent-v0.12.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.11.0...prism-agent-v0.12.0) (2022-12-06)


### Features

* **prism-agent:** implement get DIDs endpoint ([#198](https://github.com/input-output-hk/atala-prism-building-blocks/issues/198)) ([d5e08ab](https://github.com/input-output-hk/atala-prism-building-blocks/commit/d5e08ab16aa629805264a726c2cb9a5803226703))

# [prism-agent-v0.11.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.10.0...prism-agent-v0.11.0) (2022-12-06)


### Features

* **prism-agent:** verification policies pagination. ATL-1334 ([#205](https://github.com/input-output-hk/atala-prism-building-blocks/issues/205)) ([403eb38](https://github.com/input-output-hk/atala-prism-building-blocks/commit/403eb3821daab718d97f0760ba2d71e065258665))

# [prism-agent-v0.10.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.9.0...prism-agent-v0.10.0) (2022-12-02)


### Features

* **prism-agent:** fix prism-agent major issues ([#199](https://github.com/input-output-hk/atala-prism-building-blocks/issues/199)) ([1dc7339](https://github.com/input-output-hk/atala-prism-building-blocks/commit/1dc733909374ba31f4655003dafcf4479fddc70b))

# [prism-agent-v0.9.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.8.0...prism-agent-v0.9.0) (2022-12-02)


### Features

* **prism-agent:** implement pagination with navigation for schema-registry ([#195](https://github.com/input-output-hk/atala-prism-building-blocks/issues/195)) ([726e2d9](https://github.com/input-output-hk/atala-prism-building-blocks/commit/726e2d95ae879867d0932d93ac51e75b177bcb6c))

# [prism-agent-v0.8.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.7.0...prism-agent-v0.8.0) (2022-12-02)


### Features

* **prism-agent:** implement DID resolution endpoint ([#184](https://github.com/input-output-hk/atala-prism-building-blocks/issues/184)) ([7fba9b0](https://github.com/input-output-hk/atala-prism-building-blocks/commit/7fba9b0bd4eac3c5b524a6db0590abcd97839afc))

# [prism-agent-v0.7.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.6.0...prism-agent-v0.7.0) (2022-12-01)


### Features

* **pollux:** alight the OAS for schema registry ([#189](https://github.com/input-output-hk/atala-prism-building-blocks/issues/189)) ([4528c57](https://github.com/input-output-hk/atala-prism-building-blocks/commit/4528c573fc89b9af60f6bc014e7f34433a181cb8))
* **pollux:** cleanup the code of IssueCredentialApi ([16d5fdb](https://github.com/input-output-hk/atala-prism-building-blocks/commit/16d5fdbadf20c1597bf42b4e366f71623804dfc4))
* **pollux:** cleanup the OAS from Issue Credentials and other unused tags ([79170f8](https://github.com/input-output-hk/atala-prism-building-blocks/commit/79170f8722053de9e477118f3f9443c97f27c512))
* **prism-agent:** upgrade castor on prism-agent ([#141](https://github.com/input-output-hk/atala-prism-building-blocks/issues/141)) ([e85e7c0](https://github.com/input-output-hk/atala-prism-building-blocks/commit/e85e7c09019f3e1d55f36b73fa081f42c19d4218))

# [prism-agent-v0.6.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.5.0...prism-agent-v0.6.0) (2022-11-29)


### Features

* **prism-agent:** add connect/issue doc + allow local execution of multiple Prism Agent instances ([#178](https://github.com/input-output-hk/atala-prism-building-blocks/issues/178)) ([dc8d86b](https://github.com/input-output-hk/atala-prism-building-blocks/commit/dc8d86b1ba87d747c5ac0089573ddd8c2ab62f5e))

# [prism-agent-v0.5.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.4.0...prism-agent-v0.5.0) (2022-11-28)


### Features

* **pollux:** schema registry lookup and verification policies REST API ATL-1334 ([#168](https://github.com/input-output-hk/atala-prism-building-blocks/issues/168)) ([d75b36b](https://github.com/input-output-hk/atala-prism-building-blocks/commit/d75b36bd472c1cc2f15d775596a2675cda469754))
* **prism-agent:** implement Connect flow ([#130](https://github.com/input-output-hk/atala-prism-building-blocks/issues/130)) ([f7cba3b](https://github.com/input-output-hk/atala-prism-building-blocks/commit/f7cba3b18b1e03cd51db144519412fbbbe1585d0))
* **prism-agent:** make didcomm service url configurable ([#173](https://github.com/input-output-hk/atala-prism-building-blocks/issues/173)) ([b162172](https://github.com/input-output-hk/atala-prism-building-blocks/commit/b16217271e38c30cf680e193121baa7122fff67f))

# [prism-agent-v0.4.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.3.0...prism-agent-v0.4.0) (2022-11-24)


### Features

* **apollo:** add schema registry to the agent using Tapir library. ATL-1334  ([#94](https://github.com/input-output-hk/atala-prism-building-blocks/issues/94)) ([b3cf828](https://github.com/input-output-hk/atala-prism-building-blocks/commit/b3cf828d001f7499c414e9dc559f5152997445e6))

# [prism-agent-v0.3.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.2.0...prism-agent-v0.3.0) (2022-11-21)


### Features

* **prism-agent:** implement Issue Credential v2 protocol ([#146](https://github.com/input-output-hk/atala-prism-building-blocks/issues/146)) ([f3cb60e](https://github.com/input-output-hk/atala-prism-building-blocks/commit/f3cb60eb0d4dce73025326d64a012e580f581a7c)), closes [#92](https://github.com/input-output-hk/atala-prism-building-blocks/issues/92)

# [prism-agent-v0.2.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.1.0...prism-agent-v0.2.0) (2022-11-15)


### Features

* **prism-agent:** implement Flyway migrations from castor and pollux library and call on agent startup ([#117](https://github.com/input-output-hk/atala-prism-building-blocks/issues/117)) ([67bd340](https://github.com/input-output-hk/atala-prism-building-blocks/commit/67bd340e25e4a44118398746268ee25f5cf09477))

# [prism-agent-v0.1.0](https://github.com/input-output-hk/atala-prism-building-blocks/compare/prism-agent-v0.0.1...prism-agent-v0.1.0) (2022-11-11)


### Bug Fixes

* **prism-agent:** reuse db connection for background job ([#102](https://github.com/input-output-hk/atala-prism-building-blocks/issues/102)) ([a873090](https://github.com/input-output-hk/atala-prism-building-blocks/commit/a873090fdd2560eb78263060d502946f142b0574))


### Features

* **agent:** [ATL-2005] implement REST API for credential issuance ([#86](https://github.com/input-output-hk/atala-prism-building-blocks/issues/86)) ([7c1f50a](https://github.com/input-output-hk/atala-prism-building-blocks/commit/7c1f50ab99879beed74c5e0bd03aa51709051527))
* **agent:** integrate key-manage into prism-agent server ([#77](https://github.com/input-output-hk/atala-prism-building-blocks/issues/77)) ([4a88ded](https://github.com/input-output-hk/atala-prism-building-blocks/commit/4a88ded408192d03b744309a4ebaf9f9517a9db2))
* define key-management interface (3) ([#71](https://github.com/input-output-hk/atala-prism-building-blocks/issues/71)) ([47dc3cd](https://github.com/input-output-hk/atala-prism-building-blocks/commit/47dc3cd8857971b96a88ae6f9cf0e2163e6cf08e))
* **prism-agent; mercury; pollux:** Integrate Mercury into prism-agent ATL-2077; ATL-2076 ([#93](https://github.com/input-output-hk/atala-prism-building-blocks/issues/93)) ([db4b21a](https://github.com/input-output-hk/atala-prism-building-blocks/commit/db4b21ac1d6a2c48af502597779acb82f5e03ac0))
* **shared:** Add environmnet configuration for Iris DB and bump scala version in other components to enable build ([#96](https://github.com/input-output-hk/atala-prism-building-blocks/issues/96)) ([a5b583f](https://github.com/input-output-hk/atala-prism-building-blocks/commit/a5b583f445b7efd31987cf9ca017bc544a877986))
