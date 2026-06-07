package kosmoscan.analysis.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileTextExtractor {

    public String extractText(String filePath, String extension) {
        try {
            Path path = Path.of(filePath);
            if (!Files.exists(path)) {
                return "";
            }

            return switch (extension.toLowerCase()) {
                case "txt" -> Files.readString(path);
                case "pdf" -> extractFromPdf(path);
                case "docx" -> extractFromDocx(path);
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }

    private String extractFromPdf(Path path) throws Exception {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractFromDocx(Path path) throws Exception {
        try (InputStream is = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(is)) {
            StringBuilder text = new StringBuilder();
            document.getParagraphs().forEach(p -> text.append(p.getText()).append("\n"));
            return text.toString();
        }
    }
}