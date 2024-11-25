private void saveMetadataCsv(Path csvFilePath, List<String[]> metadataEntries) {
    try (BufferedWriter writer = Files.newBufferedWriter(csvFilePath)) {
        for (String[] entry : metadataEntries) {
            writer.write(String.join(",", entry));
            writer.newLine();
        }
        System.out.println("Metadata saved as CSV: " + csvFilePath.toString());
    } catch (IOException e) {
        System.out.println("Error while saving metadata.csv");
        e.printStackTrace();
    }
}
