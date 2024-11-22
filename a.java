Path sourceFolderPath = Paths.get(incomingFilePath, this.folderName);
try (FileChannel channel = FileChannel.open(sourceFolderPath, StandardOpenOption.WRITE)) {
    channel.lock().release();
    System.out.println("Released lock on source folder: " + sourceFolderPath);
} catch (IOException e) {
    System.err.println("Failed to release lock on source folder: " + sourceFolderPath);
    e.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Failed to release lock on source folder: " + e.getMessage());
}
