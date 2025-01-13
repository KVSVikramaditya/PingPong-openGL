 public static void uploadFile(String filePath, String authToken) throws IOException {
        // Create the OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Define the file and read its bytes
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Define the boundary
        String boundary = "011000010111000001101001";

        // Create the multipart request body
        RequestBody body = new MultipartBody.Builder(boundary)
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "content", 
                        file.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), fileBytes)
                )
                .build();

        // Build the HTTP request
        Request request = new Request.Builder()
                .url("https://api.seismic.com/integration/v2/teamsites/03793088-6c8c-40f5-9cb4-e930491dab43/files/b5b7727e-18d5-4b48-9f78-62b7111468cb/content")
                .put(body)
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("accept", "application/json; charset=utf-8")
                .addHeader("content-type", "multipart/form-data; boundary=" + boundary)
                .build();

        // Execute the request and get the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }
            System.out.println("Response: " + response.body().string());
        }
    }
}
