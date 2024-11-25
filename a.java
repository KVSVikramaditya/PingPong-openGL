@Component
public class CSVReaderUtil {

    public static List<MetadataSourceFile> parseMetadata(BufferedReader reader) throws IOException, CsvValidationException {
        List<MetadataSourceFile> metadataList = new ArrayList<>();
        
        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] line;

            // Skip the header line
            csvReader.readNext();

            // Read and parse each line
            while ((line = csvReader.readNext()) != null) {
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
