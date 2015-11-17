package test.others.wizzardo;

import com.wizzardo.http.HttpConnection;
import com.wizzardo.http.framework.WebApplication;
import com.wizzardo.http.request.ByteTree;
import com.wizzardo.http.request.Header;
import com.wizzardo.http.request.Request;
import com.wizzardo.http.response.JsonResponseHelper;

public class TestWizzardoTFB {

    public static final byte[] HELLO_WORLD = "Hello, World!".getBytes();

    public static class Message {
        public String message;

        public Message(String message) {
            this.message = message;
        }
    }

    public static void main(String[] args) {
        WebApplication webApplication = new WebApplication(args) {
            @Override
            protected void initHttpPartsCache() {
                ByteTree tree = httpStringsCache.getTree();
                for (Request.Method method : Request.Method.values()) {
                    tree.append(method.name());
                }
                tree.append(HttpConnection.HTTP_1_1);
            }

            @Override
            protected void setupApplication() {
                super.setupApplication();
                super.setDebugOutput(false);
                super.setOnlyCachedHeaders(true);
            }
        };

        webApplication.onSetup(app -> {
            app.getUrlMapping()
                    .append("/plaintext",
                            (request, response) -> response.setBody(HELLO_WORLD)
                                    .appendHeader(Header.KV_CONTENT_TYPE_TEXT_PLAIN))
                    .append("/json", (request, response) -> response
                            .setBody(JsonResponseHelper.renderJson(new Message("Hello, World!")))
                            .appendHeader(Header.KV_CONTENT_TYPE_APPLICATION_JSON));
        });

        webApplication.start();
    }
}
