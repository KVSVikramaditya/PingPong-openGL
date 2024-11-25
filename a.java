@Component
public class CSVReaderUtil {

    public static List<MetadataSourceFile> parseMetadata(String filePath) throws IOException, CsvValidationException {
        List<MetadataSourceFile> metadataList = new ArrayList<>();
        
        // Use try-with-resources to ensure the CSVReader and FileReader are closed properly
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            
            // Skip the header line
            reader.readNext();
            
            // Read and parse each line
            while ((line = reader.readNext()) != null) {
                MetadataSourceFile metadata = new MetadataSourceFile();
                metadata.setFilename(line[0]);
                metadata.setFiletype(line[1]);
                metadata.setSourceContentId(line[2]);
                metadata.setSourceLanguage(line[3]);
                metadata.setTargetLanguage(line[4]);
                metadataList.add(metadata);
            }
        }
        
        return metadataList;
    }
}
