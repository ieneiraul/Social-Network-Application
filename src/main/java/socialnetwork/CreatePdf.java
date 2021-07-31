package socialnetwork;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class CreatePdf {
    public void creare(List<String> lista, String dest, String numeRaport) {
        final String destinatie = dest;
        try {
            PDDocument pdDoc = new PDDocument();
            PDPage page = new PDPage();
            pdDoc.addPage(page);
            try(PDPageContentStream cs = new PDPageContentStream(pdDoc, page)){
                int ok=1;
                cs.setFont(PDType1Font.TIMES_BOLD, 18);
                cs.setNonStrokingColor(Color.black);
                cs.beginText();
                cs.newLineAtOffset(130, 740);
                cs.showText(numeRaport);
                cs.newLineAtOffset(-60, -50);
                cs.setFont(PDType1Font.TIMES_ROMAN, 16);
                for(String s: lista) {
                    if(s.startsWith("Prietenii noi")|| s.startsWith("Mesaje noi")|| s.startsWith("Mesajele primite")){
                        cs.setFont(PDType1Font.TIMES_BOLD, 16);
                        //cs.moveTextPositionByAmount(25,0);
                        cs.newLineAtOffset(25,0);
                        cs.showText(s);
                        cs.setFont(PDType1Font.TIMES_ROMAN, 16);
                        cs.newLineAtOffset(-25, -25);
                    }
                    else {
                        cs.showText(s);
                        cs.newLineAtOffset(0, -25);
                    }
                }
                cs.endText();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pdDoc.save(destinatie);
            pdDoc.close();
        } catch(IOException e) {
           e.printStackTrace();
        }
    }
}