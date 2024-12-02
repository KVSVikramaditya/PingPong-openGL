  return new com.google.gson.JsonParser()
                        .parse(response.getBody())
                        .getAsJsonObject()
                        .get("access_token")
                        .getAsString();
