package io.iohk.atala.keycloak;

import io.iohk.atala.keycloak.model.NonceRequest;
import io.iohk.atala.keycloak.model.NonceResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.logging.Logger;
import org.keycloak.util.JsonSerialization;
import org.yaml.snakeyaml.error.MissingEnvironmentVariableException;

import java.io.IOException;
import java.util.function.Supplier;

public class IdentusClient {

    private static final Logger logger = Logger.getLogger(IdentusClient.class);

    private final String identusUrl;

    private final Supplier<CloseableHttpClient> httpClient = IdentusClient::newCloseableHttpClient;


    public IdentusClient() {
        this.identusUrl = System.getenv("IDENTUS_URL");
        if (this.identusUrl == null) {
            throw new NullPointerException("The URL of identus client is null. The IDENTUS_URL environment variable is not set.");
        }
    }

    public static CloseableHttpClient newCloseableHttpClient() {
        return HttpClientBuilder.create().build();
    }

    public NonceResponse syncTokenDetails(String issuerState) {
        try (CloseableHttpClient client = httpClient.get()) {
            HttpPost post = new HttpPost(identusUrl + "/oid4vci/issuers/08877304-a111-4fe4-a84c-8666610b1665/nonces"); // TODO: use real issuer URL
            post.setEntity(new StringEntity(JsonSerialization.writeValueAsString(new NonceRequest(issuerState)), ContentType.APPLICATION_JSON));
            return NonceResponse.fromResponse(client.execute(post));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}