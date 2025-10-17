Of course. Here is the documentation for the acknowledgment endpoint, explaining its purpose and structure.

-----

## **API Documentation: Acknowledge Downloads**

### **Overview ✅**

This endpoint is the final and crucial step in the translation workflow. After you have successfully downloaded translated files using the **Download Translated Files** endpoint, you must call this endpoint to acknowledge that you have received them.

The primary reason for this acknowledgment is to prevent the system from sending you the same completed `targetId`s again. Once a `targetId` is acknowledged, it will no longer appear in the results when you query for `PROCESSED` files using the **Get Submission Status** endpoint. This prevents duplicate downloads and ensures you only process each completed file once.

### **Request Body Structure**

The request requires a JSON object containing your `userId` and a list of the `targetId`s you are acknowledging.

```json
{
  "userId": "vikramad",
  "completedTargetIds": [
    "108525",
    "108526"
  ]
}
```

-----

### **Attribute Definitions**

#### `userId`

This is a **required** string that identifies you as the client.

#### `completedTargetIds`

This is a **required** array of strings. This array should contain the `targetId`s of all the files you have successfully downloaded and processed. The service will mark each of these IDs as "completed" in the system.

### **Result of a Successful Request**

Upon a successful request, the translation project will update its records for the provided `targetId`s. These IDs will be considered fully delivered and will be excluded from future responses from the **Get Submission Status** endpoint.
