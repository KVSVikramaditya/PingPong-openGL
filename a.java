import okhttp3.*;

public class MultipartRequestWithoutFile {

    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // Define boundary
        String boundary = "011000010111000001101001";

        // Construct the multipart body manually
        String multipartBody = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"content\"\r\n\r\n" +
                "fileByteArrayContentHere\r\n" +
                "--" + boundary + "--";

        // Create the RequestBody
        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=" + boundary);
        RequestBody body = RequestBody.create(mediaType, multipartBody);

        // Build the request
        Request request = new Request.Builder()
                .url("https://api.seismic.com/integration/v2/teamsites/03793088-6c8c-40f5-9cb4-e930491dab43/files/b5b7727e-18d5-4b48-9f78-62b7111468cb/content")
                .put(body)
                .addHeader("accept", "application/json; charset=utf-8")
                .addHeader("content-type", "multipart/form-data; boundary=" + boundary)
                .build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Response: " + response.body().string());
            } else {
                System.err.println("Failed with HTTP code: " + response.code());
            }
        }
    }
}
