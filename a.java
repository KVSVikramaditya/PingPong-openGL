   // Move the folder to another location after submission is done
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                Path sourceFolderPath = Paths.get(sourceFilePath + folderName);
                Path targetFolderPath = Paths.get(sourceFilePath + "processed/" + folderName);
                Files.createDirectories(targetFolderPath.getParent());
                Files.move(sourceFolderPath, targetFolderPath);
                System.out.println("Folder moved to: " + targetFolderPath.toString());
            } catch (IOException e) {
                throw new RuntimeException("Failed to move folder: " + folderName, e);
            }
        }
