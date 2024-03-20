package models

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import java.util.Base64


class JwtCredential(base64: String) {
    private val payload: DocumentContext

    init {
        val jwt = String(Base64.getDecoder().decode(base64))
        val parts = jwt.split(".")
        payload = JsonPath.parse(String(Base64.getUrlDecoder().decode(parts[1])))
    }

    fun statusListId(): String {
        val listUrl = payload.read<String>("$.vc.credentialStatus.statusListCredential")
        return listUrl.split("/credential-status/")[1]
    }

}
