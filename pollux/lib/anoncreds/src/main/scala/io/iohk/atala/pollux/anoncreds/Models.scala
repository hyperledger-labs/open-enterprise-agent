package io.iohk.atala.pollux.anoncreds

import uniffi.anoncreds.{
  Nonce,
  Credential as UniffiCredential,
  CredentialRequests as UniffiCredentialRequests,
  CredentialDefinition as UniffiCredentialDefinition,
  CredentialDefinitionPrivate as UniffiCredentialDefinitionPrivate,
  CredentialKeyCorrectnessProof as UniffiCredentialKeyCorrectnessProof,
  CredentialOffer as UniffiCredentialOffer,
  CredentialRequest as UniffiCredentialRequest,
  CredentialRequestMetadata as UniffiCredentialRequestMetadata,
  LinkSecret as UniffiLinkSecret,
  Schema as UniffiSchema,
  Presentation as UniffiPresentation,
  PresentationRequest as UniffiPresentationRequest,
}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import scala.jdk.CollectionConverters.*
type AttributeNames = Set[String]
type IssuerId = String

case class LinkSecretWithId(id: String, secret: LinkSecret) { def data = secret.data }
object LinkSecretWithId {
  def apply(id: String): LinkSecretWithId = LinkSecretWithId(id, LinkSecret())
}

case class LinkSecret(data: String)
object LinkSecret {

  def apply(): LinkSecret = LinkSecret.given_Conversion_UniffiLinkSecret_LinkSecret(UniffiLinkSecret())

  given Conversion[LinkSecret, UniffiLinkSecret] with {
    def apply(linkSecret: LinkSecret): UniffiLinkSecret =
      UniffiLinkSecret.Companion.newFromValue(linkSecret.data)

  }

  given Conversion[UniffiLinkSecret, LinkSecret] with {
    def apply(uniffiLinkSecret: UniffiLinkSecret): LinkSecret =
      LinkSecret.apply(uniffiLinkSecret.getValue())

  }
}

//FIXME use same names as in https://hyperledger.github.io/anoncreds-spec/#term:schemas
case class SchemaDef(
    name: String, // SCHEMA_ID
    version: String, // SCHEMA_Version
    attributes: AttributeNames,
    issuer_id: IssuerId, // ISSUER_DID
)

object SchemaDef {

  given Conversion[SchemaDef, UniffiSchema] with {
    def apply(schemaDef: SchemaDef): UniffiSchema =
      UniffiSchema.apply(
        schemaDef.name,
        schemaDef.version,
        schemaDef.attributes.toSeq.asJava,
        schemaDef.issuer_id
      )

  }

  given Conversion[UniffiSchema, SchemaDef] with {
    def apply(schema: UniffiSchema): SchemaDef =
      SchemaDef.apply(
        name = schema.getName(),
        version = schema.getVersion(),
        attributes = schema.getAttrNames().asScala.toSet,
        issuer_id = schema.getIssuerId(),
      )

  }
}

// ****************************************************************************

//   {
//   "schemaId": "mock:uri2",
//   "type": "CL",
//   "tag": "tag",
//   "value": {
//     "primary": {
//       "n": "83373106802463192129400411101741921665884240313686552943065347340636028248387444457316647988280465869071434578227256762026861661212924615611239793699344659525224514203101001128754543510762437408315643323633958332029812253570449367675523015446833128246859476936428821854202450402157825876496092284296745783707365977835927218283348980141185359820866955277904067513672307803549325901880055983620476653483752449821739601185660685695995126219535143504670763603867538509721174700305155350510007049740439268393078318446525355612349994799076644707489610246610243695443811780755208493295025914421194311111805466576576734017317",
//       "s": "33892967411783178761814267006323841258270065696001175423122651756272654405659750200777791140836465942620405084110689621984154385698114722307373189847398423892407528059588413283365522639011886788273011026747741916411967859854355939457200239593603993650361636153895882494907083740595095797303547419391574910177590930357072588951622340094816619789189301104966404814420601630203700154089775605725088759237237520177412289854198242461890878426934025889073489060063186243946391328903580819940531518972212734863807973171302546576944545826388104198503347133333280648291971229845412124166607604239767231002401624400742172764644",
//       "r": {
//         "b": "705575948451630532948258636205826927386179256254252850982998136863488008236147176085017252171278892757943822269585008748285984810403777759213796182443694626845039628484662020401552973215942491981953682452032604491631457210811736533348969798515762107778354595134373965511807954374349121727371936883180079322569986838695709689906781891975409644610723318622259238762812551794952893378473709719780159148010924536926864972544451173530011392264385790283353354949867784618375671862272413558627852021411903879254380877104026237251528023889358280652600660323152609510929385592659866911592437340793190282932989723209937182029",
//         "master_secret": "53095539385114549610092210971890663242907427803660373576409214183872746638281671579167394068256310765972120268142397283953726416700758655179741943170874487115056509786135153050762587103172057217557413681991489105078039730355253025241022853711914748865926887753582560593221482601180208771876345873535495532451096207243984434063259317384635676666038975236301790263954546242992851481108342260229512721241808113827260670677107327636669057432587193123819965896339938631914642618501506125251953138829772210910976553764229660895275748373067692924612729159685110615832335670525978202293307069090312768527576171743348985673260",
//         "a": "78042608589183034606837533074064332917857863362262863514653124281746574084855802644310156626668482973886209415505517471092823228738752928374706429972350754794229351519185213917320416264828830620315237672810823348243186898434447523257299515240389177670478354210542556304869457448042017088027633177570309873736880266522827117916486606351796347221377171160386702784086222163766633645506563496869604321909169730953264576096562514712737730569423802854298526253144119368103323873312594113008203775546148729444143453156145729263021859858038995447742609772713351836276294259408956564690305226834791877257156417883620628503009",
//         "c": "44548496496089088309033701514198600589269882940758166806015718896967828602426334747247212450185262811513614218187272969278529812386438232813368940055438155190216775868519237254933518461958382345189983861926241107342517226009511217953085265297840176670549900729445855211397771179787331403614253151824945438543756798466993261624342422990488885917749777678282811713636017308766641347411604494516802581622518860236926432493743840677735784476875787635356539468078198849645236412621416887452957866613063353139866328316523939777282993538733854400960916878335473863920644098095424448802679695667206317436441291642308537282388"
//       },
//       "rctxt": "77524955121382896270629471526889480524444498368394149921377362089261075535729271230114060251948728287606384526275691602095196496465334741675641441303739543568945850246504368549146958244456604964757817120350352715645933675145196490357341712808708469678479427539954291630432160786741120809408088729544123358045420308227703925253140727131305327348946357455153639170367090387346813419585787100653715622872220423574120457396975961119035734036132242103752092062217068588273763525927168483221657998067718168249819580770932728761738438954389382533701244531968978085101930983662561437949972980079811116188036916908292890859841",
//       "z": "68048655111565362294933879232079610074841432465559584826389645688555480410030773929139911763905183564105733200159262559275610312799434186969343270258332075188876386732730122227963757973481413958114083293847547736463817430769536806867478390402439386935822065426026076319082011086015540049178063268459582659008300901681505485925169921373847000362497529372496731419631183546939278046455804224961086571687888762251604782869386000899986521772817579044176080396773468715901176409747025519043231153513447693227590292820602669447563721487897951609925287374232779031225316614791235786039515080292709424638683136700776604515832"
//     }
//   },
//   "issuerId": "mock:issuer_id/path&q=bar"
// }
// case class CredentialDefinition(
//     schemaId: String,
//     `type`: String,
//     tag: String,
//     value: String,
//     issuerId: String,
// )
case class CredentialDefinition(data: String) { // TODO
  def schemaId = CredentialDefinition
    .given_Conversion_CredentialDefinition_UniffiCredentialDefinition(this)
    .getSchemaId()
}
object CredentialDefinition {
  given Conversion[CredentialDefinition, UniffiCredentialDefinition] with {
    def apply(credentialDefinition: CredentialDefinition): UniffiCredentialDefinition =
      UniffiCredentialDefinition(credentialDefinition.data)
  }

  given Conversion[UniffiCredentialDefinition, CredentialDefinition] with {
    def apply(credentialDefinition: UniffiCredentialDefinition): CredentialDefinition =
      CredentialDefinition(credentialDefinition.getJson())
  }
}

// ****************************************************************************

// {
//   "value": {
//     "p_key": {
//       "p": "146316020969219156418059217943022704761402341071565944162767588551501826802811674960888666183894183313662159509014680163555103929497156721856250190170787066488782015585851994840526996581809279379330798584455231875571708539659845698845702869399073363904711385397153770568203822288706117362168916264301835828553",
//       "q": "145171499655033934725136026840910556338711630377902223746779305000980761289207462788824179430484927982519467102926056791461043017278231772145863605806371955315590836939459191201273156939782387695003898903616834570434160260053887416123229709637273159937475067228936598913341722510388224425748400570316681897369"
//     },
//     "r_key": null
//   }
// }
case class CredentialDefinitionPrivate(data: String)
object CredentialDefinitionPrivate {
  given Conversion[CredentialDefinitionPrivate, UniffiCredentialDefinitionPrivate] with {
    def apply(credentialDefinitionPrivate: CredentialDefinitionPrivate): UniffiCredentialDefinitionPrivate =
      UniffiCredentialDefinitionPrivate(credentialDefinitionPrivate.data)
  }

  given Conversion[UniffiCredentialDefinitionPrivate, CredentialDefinitionPrivate] with {
    def apply(credentialDefinitionPrivate: UniffiCredentialDefinitionPrivate): CredentialDefinitionPrivate =
      CredentialDefinitionPrivate(credentialDefinitionPrivate.getJson())
  }
}

// ****************************************************************************

case class CredentialKeyCorrectnessProof(data: String)
object CredentialKeyCorrectnessProof {
  given Conversion[CredentialKeyCorrectnessProof, UniffiCredentialKeyCorrectnessProof] with {
    def apply(credentialKeyCorrectnessProof: CredentialKeyCorrectnessProof): UniffiCredentialKeyCorrectnessProof =
      UniffiCredentialKeyCorrectnessProof(credentialKeyCorrectnessProof.data)
  }

  given Conversion[UniffiCredentialKeyCorrectnessProof, CredentialKeyCorrectnessProof] with {
    def apply(credentialKeyCorrectnessProof: UniffiCredentialKeyCorrectnessProof): CredentialKeyCorrectnessProof =
      CredentialKeyCorrectnessProof(credentialKeyCorrectnessProof.getJson())
  }
}

case class CreateCredentialDefinition(
    cd: CredentialDefinition,
    cdPrivate: CredentialDefinitionPrivate,
    proofKey: CredentialKeyCorrectnessProof,
)
// ****************************************************************************

case class CredentialOffer(data: String) {
  lazy val schemaId = CredentialOffer
    .given_Conversion_CredentialOffer_UniffiCredentialOffer(this)
    .getSchemaId()
  lazy val credDefId = CredentialOffer
    .given_Conversion_CredentialOffer_UniffiCredentialOffer(this)
    .getCredDefId()
}
object CredentialOffer {
  given Conversion[CredentialOffer, UniffiCredentialOffer] with {
    def apply(credentialOffer: CredentialOffer): UniffiCredentialOffer =
      UniffiCredentialOffer(credentialOffer.data)
  }

  given Conversion[UniffiCredentialOffer, CredentialOffer] with {
    def apply(credentialOffer: UniffiCredentialOffer): CredentialOffer =
      CredentialOffer(credentialOffer.getJson())
  }
}

// ****************************************************************************

case class CreateCrendentialRequest(
    request: CredentialRequest,
    metadata: CredentialRequestMetadata
)

case class CredentialRequest(data: String)
object CredentialRequest {

  given Conversion[CredentialRequest, UniffiCredentialRequest] with {
    def apply(credentialRequest: CredentialRequest): UniffiCredentialRequest =
      UniffiCredentialRequest(credentialRequest.data)
  }

  given Conversion[UniffiCredentialRequest, CredentialRequest] with {
    def apply(credentialRequest: UniffiCredentialRequest): CredentialRequest =
      CredentialRequest(credentialRequest.getJson())
  }
}

case class CredentialRequestMetadata(
    linkSecretBlinding: String,
    nonce: String,
    linkSecretName: String,
)
object CredentialRequestMetadata {
  given Conversion[CredentialRequestMetadata, UniffiCredentialRequestMetadata] with {
    def apply(credentialRequestMetadata: CredentialRequestMetadata): UniffiCredentialRequestMetadata =
      UniffiCredentialRequestMetadata(
        /*link_secret_blinding_data*/ credentialRequestMetadata.linkSecretBlinding,
        /*nonce*/ Nonce.Companion.newFromValue(credentialRequestMetadata.nonce),
        /*link_secret_name*/ credentialRequestMetadata.linkSecretName,
      )
  }

  given Conversion[UniffiCredentialRequestMetadata, CredentialRequestMetadata] with {
    def apply(credentialRequestMetadata: UniffiCredentialRequestMetadata): CredentialRequestMetadata =
      CredentialRequestMetadata(
        linkSecretBlinding = credentialRequestMetadata.getLinkSecretBlindingData(),
        nonce = credentialRequestMetadata.getNonce().getValue(),
        linkSecretName = credentialRequestMetadata.getLinkSecretName(),
      )
  }

  given JsonDecoder[CredentialRequestMetadata] = DeriveJsonDecoder.gen[CredentialRequestMetadata]
  given JsonEncoder[CredentialRequestMetadata] = DeriveJsonEncoder.gen[CredentialRequestMetadata]
}

// ****************************************************************************

//Credential
case class Credential(data: String) {
  lazy val credDefId: String = Credential
    .given_Conversion_Credential_UniffiCredential(this)
    .getCredDefId
}
object Credential {
  given Conversion[Credential, UniffiCredential] with {
    def apply(credential: Credential): UniffiCredential =
      UniffiCredential(credential.data)
  }

  given Conversion[UniffiCredential, Credential] with {
    def apply(credential: UniffiCredential): Credential =
      Credential(credential.getJson())
  }
}

// ****************************************************************************
case class CredentialAndRequestedAttributesPredicates(
    credential: Credential,
    requestedAttribute: Seq[String],
    requestedPredicate: Seq[String],
)

object CredentialAndRequestedAttributesPredicates {
  given Conversion[CredentialAndRequestedAttributesPredicates, UniffiCredentialRequests] with {
    import uniffi.anoncreds.RequestedAttribute
    import uniffi.anoncreds.RequestedPredicate
    def apply(credentialRequests: CredentialAndRequestedAttributesPredicates): UniffiCredentialRequests = {
      val credential = Credential.given_Conversion_Credential_UniffiCredential(credentialRequests.credential)
      val requestedAttributes = credentialRequests.requestedAttribute.map(a => RequestedAttribute(a, true))
      val requestedPredicates = credentialRequests.requestedPredicate.map(p => RequestedPredicate(p))
      UniffiCredentialRequests(credential, requestedAttributes.asJava, requestedPredicates.asJava)
    }
  }

  given Conversion[UniffiCredentialRequests, CredentialAndRequestedAttributesPredicates] with {
    def apply(credentialRequests: UniffiCredentialRequests): CredentialAndRequestedAttributesPredicates = {
      CredentialAndRequestedAttributesPredicates(
        Credential.given_Conversion_UniffiCredential_Credential(credentialRequests.getCredential()),
        credentialRequests
          .getRequestedAttribute()
          .asScala
          .toSeq
          .filter(e => e.getRevealed())
          .map(e => e.getReferent()),
        credentialRequests
          .getRequestedPredicate()
          .asScala
          .toSeq
          .map(e => e.getReferent())
      )
    }
  }
}

//UniffiCredentialRequests

// ****************************************************************************

case class PresentationRequest(data: String)
object PresentationRequest {
  given Conversion[PresentationRequest, UniffiPresentationRequest] with {
    def apply(presentationRequest: PresentationRequest): UniffiPresentationRequest =
      UniffiPresentationRequest(presentationRequest.data)
  }

  given Conversion[UniffiPresentationRequest, PresentationRequest] with {
    def apply(presentationRequest: UniffiPresentationRequest): PresentationRequest =
      PresentationRequest(presentationRequest.getJson())
  }
}

// ****************************************************************************

case class Presentation(data: String)
object Presentation {
  given Conversion[Presentation, UniffiPresentation] with {
    def apply(presentation: Presentation): UniffiPresentation =
      UniffiPresentation(presentation.data)
  }

  given Conversion[UniffiPresentation, Presentation] with {
    def apply(presentation: UniffiPresentation): Presentation =
      Presentation(presentation.getJson())
  }
}
