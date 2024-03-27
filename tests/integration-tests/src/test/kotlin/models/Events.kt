package models

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import io.iohk.atala.prism.models.*

data class Event(
    @SerializedName("type") var type: String,
    @SerializedName("id") var id: String,
    @SerializedName("ts") var ts: String,
    @SerializedName("data") var data: JsonElement,
    @SerializedName("walletId") var walletId: String,
)

data class ConnectionEvent(
    @SerializedName("type") var type: String,
    @SerializedName("id") var id: String,
    @SerializedName("ts") var ts: String,
    @SerializedName("data") var data: Connection,
    @SerializedName("walletId") var walletId: String,
)

data class CredentialEvent(
    @SerializedName("type") var type: String,
    @SerializedName("id") var id: String,
    @SerializedName("ts") var ts: String,
    @SerializedName("data") var data: IssueCredentialRecord,
    @SerializedName("walletId") var walletId: String,
)

data class PresentationEvent(
    @SerializedName("type") var type: String,
    @SerializedName("id") var id: String,
    @SerializedName("ts") var ts: String,
    @SerializedName("data") var data: PresentationStatusAdapter, // FIXME: rollback to PresentationStatus when Status is fixed
    @SerializedName("walletId") var walletId: String,
)

data class PresentationStatusAdapter( // FIXME: delete this class when PresentationStatus.Status is fixed
    @SerializedName("presentationId")  val presentationId: String,
    @SerializedName("thid")  val thid: String,
    @SerializedName("role") val role: PresentationStatus.Role,
    @SerializedName("status")  val status: Status,
    @SerializedName("metaRetries") val metaRetries: Int,
    @SerializedName("proofs") val proofs: List<ProofRequestAux>? = null,
    @SerializedName("data") val `data`: List<String>? = null,
    @SerializedName("connectionId") val connectionId: String? = null
) {
    enum class Status(val value: String) {
        @SerializedName(value = "RequestPending") REQUEST_PENDING("RequestPending"),
        @SerializedName(value = "RequestSent") REQUEST_SENT("RequestSent"),
        @SerializedName(value = "RequestReceived") REQUEST_RECEIVED("RequestReceived"),
        @SerializedName(value = "RequestRejected") REQUEST_REJECTED("RequestRejected"),
        @SerializedName(value = "PresentationPending") PRESENTATION_PENDING("PresentationPending"),
        @SerializedName(value = "PresentationGenerated") PRESENTATION_GENERATED("PresentationGenerated"),
        @SerializedName(value = "PresentationSent") PRESENTATION_SENT("PresentationSent"),
        @SerializedName(value = "PresentationReceived") PRESENTATION_RECEIVED("PresentationReceived"),
        @SerializedName(value = "PresentationVerified") PRESENTATION_VERIFIED("PresentationVerified"),
        @SerializedName(value = "PresentationAccepted") PRESENTATION_ACCEPTED("PresentationAccepted"),
        @SerializedName(value = "PresentationRejected") PRESENTATION_REJECTED("PresentationRejected"),
        @SerializedName(value = "ProblemReportPending") PROBLEM_REPORT_PENDING("ProblemReportPending"),
        @SerializedName(value = "ProblemReportSent") PROBLEM_REPORT_SENT("ProblemReportSent"),
        @SerializedName(value = "ProblemReportReceived") PROBLEM_REPORT_RECEIVED("ProblemReportReceived"),
        @SerializedName(value = "PresentationVerificationFailed") PRESENTATION_VERIFICATION_FAILED("PresentationVerificationFailed");
    }
}

data class DidEvent(
    @SerializedName("type") var type: String,
    @SerializedName("id") var id: String,
    @SerializedName("ts") var ts: String,
    @SerializedName("data") var data: ManagedDID,
    @SerializedName("walletId") var walletId: String,
)
