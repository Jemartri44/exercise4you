package es.codeurjc.exercise4you.service.questionnaire;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.PdfMultipartFile;
import es.codeurjc.exercise4you.entity.objectives.Objective;
import es.codeurjc.exercise4you.entity.objectives.ObjectivesResponse;
import es.codeurjc.exercise4you.entity.prescriptions.Prescription;
import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionsResponse;
import es.codeurjc.exercise4you.entity.questionnaire.Apalq;
import es.codeurjc.exercise4you.entity.questionnaire.Cmtcef;
import es.codeurjc.exercise4you.entity.questionnaire.Eparmed;
import es.codeurjc.exercise4you.entity.questionnaire.Ipaq;
import es.codeurjc.exercise4you.entity.questionnaire.Ipaqe;
import es.codeurjc.exercise4you.entity.questionnaire.Parq;
import es.codeurjc.exercise4you.entity.questionnaire.results.ApalqResults;
import es.codeurjc.exercise4you.entity.questionnaire.results.CmtcefResults;
import es.codeurjc.exercise4you.entity.questionnaire.results.EparmedResults;
import es.codeurjc.exercise4you.entity.questionnaire.results.IpaqResults;
import es.codeurjc.exercise4you.entity.questionnaire.results.IpaqeResults;
import es.codeurjc.exercise4you.entity.questionnaire.results.ParqResults;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.service.DataRecordService;
import es.codeurjc.exercise4you.service.PatientService;
import es.codeurjc.exercise4you.service.S3Service;

@Service
public class PdfService {
    @Autowired
    private PatientService patientService;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private DataRecordService dataRecordService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private Locale locale = new Locale("es", "ES");
    private DateTimeFormatter timeFormatter = DateTimeFormatter
        .ofLocalizedTime(FormatStyle.SHORT)
        .withLocale(locale);

    public MultipartFile getPdf(Integer id, String pdfType, Integer nSession) throws IOException{
        patientService.checkSession(nSession);
        patientService.checkPatient(id);
        LocalDate date = dataRecordService.getCompletionDateBySession(id, nSession);
        String filepath = "pdfs/";
        String filename = "";
        filepath = filepath + pdfType + "/";
        filename = String.valueOf(id) + "_" + pdfType.toUpperCase() + "_" + date + ".pdf";
        return s3Service.downloadMultipartFile(filepath, filename);
    }

    public MultipartFile getManual() throws IOException{
        String filepath = "pdfs/";
        String filename = "manual.pdf";
        return s3Service.downloadMultipartFile(filepath, filename);
    }

    public MultipartFile getSkinFoldsGuide() throws IOException{
        String filepath = "pdfs/";
        String filename = "skinFoldsGuide.pdf";
        return s3Service.downloadMultipartFile(filepath, filename);
    }

    public String generateIpaqPdf(Ipaq ipaq) throws DocumentException, IOException {
        Optional<Patient> optional = patientRepository.findById(ipaq.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();
        IpaqResults results = IpaqService.getResults(ipaq);
        Document document = new Document(PageSize.A4, 70, 70, 60, 30);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Informe sobre el Cuestionario internacional de actividad física (IPAQ)", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());
        
        document.add(title);
        document.add(linebreak);

        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 695); // coordinates for the left column
        DecimalFormat df = new DecimalFormat("0.00");
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), ipaq.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p3 = new Paragraph("Peso: " + df.format(ipaq.getWeight()) + " kilogramos", leftColumnFont);
        Paragraph p4 = new Paragraph("Fecha: " + ipaq.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + ipaq.getSession() + ")", leftColumnFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(3);
        p3.setSpacingAfter(3);
        p4.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p3);
        ct.addElement(p4);
        ct.go();

        ct.setSimpleColumn(340, 150, 500, 695); // coordinates for the right column
        InputStream imgStream = new ClassPathResource("img/exercise4you.png").getInputStream();
        Image img = Image.getInstance(imgStream.readAllBytes());
        img.scaleToFit(80, 80); // adjust the size as needed
        img.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        img.setSpacingBefore(20);
        ct.addElement(img);
        ct.go();

        Paragraph emptyLine = new Paragraph("");
        emptyLine.setSpacingBefore(75);
        document.add(emptyLine);
        document.add(linebreak);

        Font bodyFont = FontFactory.getFont("Helvetica", 9);
        Paragraph introduction = new Paragraph("Estimado señor/señora a continuación, puede encontrar la estimación de su compromiso metabólico semanal en términos de caminata y actividad física de moderada a vigorosa. Estos cálculos están diseñados para poder clasificar su nivel de actividad física. Esto le permite comprender si se debe aumentar la calidad y cantidad de su actividad física semanal, lo que ayudará a prevenir la aparición de patologías que se correlacionan con un estilo de vida sedentario. Este informe automático se ha generado siguiendo la evaluación y los consejos contenidos en el protocolo de puntuación del Cuestionario Internacional de Actividad Física (IPAQ).", bodyFont);
        introduction.setSpacingBefore(10);
        introduction.setLeading(12);
        introduction.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(introduction);

        // Create table
        PdfPTable table = new PdfPTable(3); // 3 columns
        table.setWidthPercentage(100); // Full Width
        table.setSpacingBefore(10f); // Space before table
        table.setSpacingAfter(7f); // Space after table


        // Data
        String[][] data = {
            {"Actividades Físicas Ligeras:", "", ""},
            {"Caminatas", results.getLightMet().toString() + " MET-min/semana", df.format(results.getLightCalories()) + " KCal/semana"},
            {"Actividades Físicas Moderadas:", "", ""},
            {"", results.getModerateMet().toString() + " MET-min/semana", df.format(results.getModerateCalories()) + " KCal/semana"},
            {"Actividades Físicas Vigorosas:", "", ""},
            {"", results.getVigorousMet().toString() + " MET-min/semana", df.format(results.getVigorousCalories()) + " KCal/semana"},
            {"Total:", results.getTotalMet().toString() + " MET-min/semana", df.format(results.getTotalCalories()) + " KCal/semana"}
        };
        BaseColor paleGreen = new BaseColor(120, 194, 122);
        BaseColor paleOrange = new BaseColor(255, 170, 90);
        BaseColor paleRed = new BaseColor(255, 120, 90);
        BaseColor paleYellow = new BaseColor(255, 220, 90);
        Font tableFont = FontFactory.getFont("Helvetica", 9);
        for (String datum : data[0]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleGreen);
            cell.setBackgroundColor(paleGreen);
            table.addCell(cell);
        }
        for (String datum : data[1]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleGreen);
            cell.setBackgroundColor(paleGreen);
            cell.setPadding(5);
            table.addCell(cell);
        }
        for (String datum : data[2]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleOrange);
            cell.setBackgroundColor(paleOrange); 
            table.addCell(cell);
        }
        for (String datum : data[3]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleOrange);
            cell.setBackgroundColor(paleOrange);
            cell.setPadding(5);
            table.addCell(cell);
        }
        for (String datum : data[4]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleRed);
            cell.setBackgroundColor(paleRed);
            table.addCell(cell);
        }
        for (String datum : data[5]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleRed);
            cell.setBackgroundColor(paleRed);
            cell.setPadding(5);
            table.addCell(cell);
        }
        for (String datum : data[6]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleYellow);
            cell.setBackgroundColor(paleYellow);
            cell.setPadding(8);
            table.addCell(cell);
        }
        document.add(table);

        Paragraph activityLevel = new Paragraph("Nivel de actividad física semanal:", bodyFont);
        activityLevel.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(activityLevel);

        PdfPTable table2 = new PdfPTable(1); // 1 columns
        table2.setWidthPercentage(100); // Full Width
        table2.setSpacingBefore(10f); // Space before table
        table2.setSpacingAfter(5f); // Space after table
        PdfPCell cell = new PdfPCell(new Paragraph(results.getActivityLevel(), tableFont));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBorderColor(paleYellow);
        cell.setBackgroundColor(paleYellow);
        cell.setPadding(7);
        table2.addCell(cell);
        document.add(table2);

        Paragraph sendentaryTime = new Paragraph("Según su informe, de lunes a viernes, su tiempo diario dedicado a actividades sedentarias es,", bodyFont);
        sendentaryTime.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(sendentaryTime);

        PdfPTable table3 = new PdfPTable(1); // 1 columns
        table3.setWidthPercentage(100); // Full Width
        table3.setSpacingBefore(10f); // Space before table
        table3.setSpacingAfter(5f); // Space after table
        cell = new PdfPCell(new Paragraph(results.getSendentaryHours() + " hora/s    " + results.getSedentaryMinutes() + " minutos/día", tableFont));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBorderColor(paleYellow);
        cell.setBackgroundColor(paleYellow);
        cell.setPadding(7);
        table3.addCell(cell);
        document.add(table3);

        Paragraph closing = new Paragraph("Como destaca la literatura científica, tanto el tiempo diario de sedentarismo como el tiempo diario dedicado a la actividad física de intensidad moderada a vigorosa tienen un papel en la aparición y/o prevención de las patologías crónicas no transmisibles más comunes, como los trastornos cardiometabólicos y ciertos tipos de cáncer, es extremadamente importante tener ambos en consideración para promover una buena salud.", bodyFont);
        closing.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        closing.setLeading(12);
        closing.setSpacingAfter(5);
        document.add(closing);

        Paragraph comentary = new Paragraph("COMENTARIO", bodyFont);
        comentary.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(comentary);

        PdfPTable table4 = new PdfPTable(1); // 1 columns
        table4.setWidthPercentage(100); // Full Width
        table4.setSpacingBefore(10f); // Space before table
        table4.setSpacingAfter(2f); // Space after table
        Paragraph recomendation = new Paragraph(results.getComment(), tableFont);
        recomendation.setLeading(14);
        cell = new PdfPCell(recomendation);
        cell.setLeading(12, 0);
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        cell.setBorderColor(paleGreen);
        cell.setBackgroundColor(paleGreen);
        cell.setPadding(7);
        table4.addCell(cell);
        document.add(table4);

        Font footerFont = FontFactory.getFont("Helvetica", 6);
        Paragraph info = new Paragraph("MET-min/semana. El Metabolic Equivalent of Task (MET), o equivalente metabólico, es una medida fisiológica que expresa el costo energético de las actividades físicas y se define como la relación de la tasa metabólica (y por lo tanto la tasa de consumo de energía) durante una actividad física específica a una tasa metabólica de referencia, generalmente representada por la tasa metabólica en reposo. En este caso, la variable MET-min/semana expresa el compromiso metabólico semanal en la caminata, y en la práctica de actividad física tanto moderada como vigorosa. \n" + //
                        "Ekelund et al. Does physical activity attenuate, or even eliminate, the detrimental association of sitting time with mortality? A harmonised meta-analysis of data from more than 1 million men and women. Lancet. 2016 Sep 24;388(10051):1302-10. ", footerFont);
        info.setAlignment(com.itextpdf.text.Element.ALIGN_BASELINE);
        info.setLeading(7);
        document.add(info);

        document.close();
        
        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_IPAQ_" + ipaq.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/ipaq/", pdfMultipartFile);
            return returned.split(" ")[3];
        } catch (Exception e) {
            throw new IOException("Error uploading file to S3");
        }
    }

    public String generateIpaqePdf(Ipaqe ipaqe) throws DocumentException, IOException {
        Optional<Patient> optional = patientRepository.findById(ipaqe.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();
        IpaqeResults results = IpaqeService.getResults(ipaqe);
        Document document = new Document(PageSize.A4, 70, 70, 60, 30);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Cuestionario internacional de actividad física en personas mayores (IPAQ-E)", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());
        
        document.add(title);
        document.add(linebreak);

        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 695); // coordinates for the left column
        DecimalFormat df = new DecimalFormat("0.00");
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), ipaqe.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p3 = new Paragraph("Peso: " + df.format(ipaqe.getWeight()) + " kilogramos", leftColumnFont);
        Paragraph p4 = new Paragraph("Fecha: " + ipaqe.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + ipaqe.getSession() + ")", leftColumnFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(3);
        p3.setSpacingAfter(3);
        p4.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p3);
        ct.addElement(p4);
        ct.go();

        ct.setSimpleColumn(340, 150, 500, 695); // coordinates for the right column
        InputStream imgStream = new ClassPathResource("img/exercise4you.png").getInputStream();
        Image img = Image.getInstance(imgStream.readAllBytes());
        img.scaleToFit(80, 80); // adjust the size as needed
        img.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        img.setSpacingBefore(20);
        ct.addElement(img);
        ct.go();

        Paragraph emptyLine = new Paragraph("");
        emptyLine.setSpacingBefore(75);
        document.add(emptyLine);
        document.add(linebreak);

        Font bodyFont = FontFactory.getFont("Helvetica", 9);
        Paragraph introduction = new Paragraph("Estimado señor/señora a continuación, puede encontrar la estimación de su compromiso metabólico semanal en términos de caminata y actividad física de moderada a vigorosa. Estos cálculos están diseñados para poder clasificar su nivel de actividad física. Esto le permite comprender si se debe aumentar la calidad y cantidad de su actividad física semanal, lo que ayudará a prevenir la aparición de patologías que se correlacionan con un estilo de vida sedentario. Este informe automático se ha generado siguiendo la evaluación y los consejos contenidos en el protocolo de puntuación del Cuestionario Internacional de Actividad Física (IPAQ-E).", bodyFont);
        introduction.setSpacingBefore(10);
        introduction.setLeading(12);
        introduction.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(introduction);

        // Create table
        PdfPTable table = new PdfPTable(3); // 3 columns
        table.setWidthPercentage(100); // Full Width
        table.setSpacingBefore(10f); // Space before table
        table.setSpacingAfter(7f); // Space after table


        // Data
        String[][] data = {
            {"Actividades Físicas Ligeras:", "", ""},
            {"Caminatas", results.getLightMet().toString() + " MET-min/semana", df.format(results.getLightCalories()) + " KCal/semana"},
            {"Actividades Físicas Moderadas:", "", ""},
            {"", results.getModerateMet().toString() + " MET-min/semana", df.format(results.getModerateCalories()) + " KCal/semana"},
            {"Actividades Físicas Vigorosas:", "", ""},
            {"", results.getVigorousMet().toString() + " MET-min/semana", df.format(results.getVigorousCalories()) + " KCal/semana"},
            {"Total:", results.getTotalMet().toString() + " MET-min/semana", df.format(results.getTotalCalories()) + " KCal/semana"}
        };
        BaseColor paleGreen = new BaseColor(120, 194, 122);
        BaseColor paleOrange = new BaseColor(255, 170, 90);
        BaseColor paleRed = new BaseColor(255, 120, 90);
        BaseColor paleYellow = new BaseColor(255, 220, 90);
        Font tableFont = FontFactory.getFont("Helvetica", 9);
        for (String datum : data[0]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleGreen);
            cell.setBackgroundColor(paleGreen);
            table.addCell(cell);
        }
        for (String datum : data[1]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleGreen);
            cell.setBackgroundColor(paleGreen);
            cell.setPadding(5);
            table.addCell(cell);
        }
        for (String datum : data[2]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleOrange);
            cell.setBackgroundColor(paleOrange); 
            table.addCell(cell);
        }
        for (String datum : data[3]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleOrange);
            cell.setBackgroundColor(paleOrange);
            cell.setPadding(5);
            table.addCell(cell);
        }
        for (String datum : data[4]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleRed);
            cell.setBackgroundColor(paleRed);
            table.addCell(cell);
        }
        for (String datum : data[5]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleRed);
            cell.setBackgroundColor(paleRed);
            cell.setPadding(5);
            table.addCell(cell);
        }
        for (String datum : data[6]) {
            PdfPCell cell = new PdfPCell(new Paragraph(datum, tableFont));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            cell.setBorderColor(paleYellow);
            cell.setBackgroundColor(paleYellow);
            cell.setPadding(8);
            table.addCell(cell);
        }
        document.add(table);

        Paragraph activityLevel = new Paragraph("Nivel de actividad física semanal:", bodyFont);
        activityLevel.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(activityLevel);

        PdfPTable table2 = new PdfPTable(1); // 1 columns
        table2.setWidthPercentage(100); // Full Width
        table2.setSpacingBefore(10f); // Space before table
        table2.setSpacingAfter(5f); // Space after table
        PdfPCell cell = new PdfPCell(new Paragraph(results.getActivityLevel(), tableFont));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBorderColor(paleYellow);
        cell.setBackgroundColor(paleYellow);
        cell.setPadding(7);
        table2.addCell(cell);
        document.add(table2);

        Paragraph sendentaryTime = new Paragraph("Según su informe, de lunes a viernes, su tiempo diario dedicado a actividades sedentarias es,", bodyFont);
        sendentaryTime.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(sendentaryTime);

        PdfPTable table3 = new PdfPTable(1); // 1 columns
        table3.setWidthPercentage(100); // Full Width
        table3.setSpacingBefore(10f); // Space before table
        table3.setSpacingAfter(5f); // Space after table
        cell = new PdfPCell(new Paragraph(results.getSendentaryHours() + " hora/s    " + results.getSedentaryMinutes() + " minutos/día", tableFont));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBorderColor(paleYellow);
        cell.setBackgroundColor(paleYellow);
        cell.setPadding(7);
        table3.addCell(cell);
        document.add(table3);

        Paragraph closing = new Paragraph("Como destaca la literatura científica, tanto el tiempo diario de sedentarismo como el tiempo diario dedicado a la actividad física de intensidad moderada a vigorosa tienen un papel en la aparición y/o prevención de las patologías crónicas no transmisibles más comunes, como los trastornos cardiometabólicos y ciertos tipos de cáncer, es extremadamente importante tener ambos en consideración para promover una buena salud.", bodyFont);
        closing.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        closing.setLeading(12);
        closing.setSpacingAfter(5);
        document.add(closing);

        Paragraph comentary = new Paragraph("COMENTARIO", bodyFont);
        comentary.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(comentary);

        PdfPTable table4 = new PdfPTable(1); // 1 columns
        table4.setWidthPercentage(100); // Full Width
        table4.setSpacingBefore(10f); // Space before table
        table4.setSpacingAfter(2f); // Space after table
        Paragraph recomendation = new Paragraph(results.getComment(), tableFont);
        recomendation.setLeading(14);
        cell = new PdfPCell(recomendation);
        cell.setLeading(12, 0);
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        cell.setBorderColor(paleGreen);
        cell.setBackgroundColor(paleGreen);
        cell.setPadding(7);
        table4.addCell(cell);
        document.add(table4);

        Font footerFont = FontFactory.getFont("Helvetica", 6);
        Paragraph info = new Paragraph("MET-min/semana. El Metabolic Equivalent of Task (MET), o equivalente metabólico, es una medida fisiológica que expresa el costo energético de las actividades físicas y se define como la relación de la tasa metabólica (y por lo tanto la tasa de consumo de energía) durante una actividad física específica a una tasa metabólica de referencia, generalmente representada por la tasa metabólica en reposo. En este caso, la variable MET-min/semana expresa el compromiso metabólico semanal en la caminata, y en la práctica de actividad física tanto moderada como vigorosa. \n" + //
                        "Ekelund et al. Does physical activity attenuate, or even eliminate, the detrimental association of sitting time with mortality? A harmonised meta-analysis of data from more than 1 million men and women. Lancet. 2016 Sep 24;388(10051):1302-10. ", footerFont);
        info.setAlignment(com.itextpdf.text.Element.ALIGN_BASELINE);
        info.setLeading(7);
        document.add(info);

        document.close();
        
        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_IPAQE_" + ipaqe.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/ipaqe/", pdfMultipartFile);
            return returned.split(" ")[3];
        } catch (Exception e) {
            throw new IOException("Error uploading file to S3");
        }
    }

    public String generateCmtcefPdf(Cmtcef cmtcef) throws DocumentException, IOException {
        Optional<Patient> optional = patientRepository.findById(cmtcef.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();
        CmtcefResults results = CmtcefService.getResults(cmtcef);
        Document document = new Document(PageSize.A4, 70, 70, 60, 30);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Informe sobre el Cuestionario del modelo transteórico del cambio del ejercicio físico", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());
        
        
        document.add(title);
        document.add(linebreak);
        
        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 695); // coordinates for the left column
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), cmtcef.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p3= new Paragraph("Fecha: " + cmtcef.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + cmtcef.getSession() + ")", leftColumnFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(3);
        p3.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p3);
        ct.go();
        
        ct.setSimpleColumn(340, 150, 500, 695); // coordinates for the right column
        InputStream imgStream = new ClassPathResource("img/exercise4you.png").getInputStream();
        Image img = Image.getInstance(imgStream.readAllBytes());
        img.scaleToFit(80, 80); // adjust the size as needed
        img.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        img.setSpacingBefore(10);
        ct.addElement(img);
        ct.go();
        
        Paragraph emptyLine = new Paragraph("");
        emptyLine.setSpacingBefore(55);
        document.add(emptyLine);
        document.add(linebreak);

        Font bodyFont = FontFactory.getFont("Helvetica", 10);
        Paragraph stage = new Paragraph("Etapa de cambio del paciente: " + results.getStage(), bodyFont);
        stage.setSpacingBefore(10);
        stage.setLeading(12);
        stage.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(stage);
        Paragraph characteristicsIntro = new Paragraph("Características del paciente:", bodyFont);
        characteristicsIntro.setSpacingBefore(25);
        characteristicsIntro.setSpacingAfter(15);
        characteristicsIntro.setLeading(12);
        characteristicsIntro.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(characteristicsIntro);

        List characteristicList = new List(List.UNORDERED);
        characteristicList.setListSymbol("\u2022  ");
        characteristicList.setIndentationLeft(20);
        characteristicList.setIndentationRight(20);
        for (String characteristic : results.getCharacteristics()) {
            ListItem item = new ListItem(characteristic, bodyFont);
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            characteristicList.add(item);
        }
        document.add(characteristicList);
        Paragraph actionsIntro = new Paragraph("Acciones para el profesional de la salud:", bodyFont);
        actionsIntro.setSpacingBefore(25);
        actionsIntro.setSpacingAfter(15);
        actionsIntro.setLeading(12);
        actionsIntro.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(actionsIntro);

        List actionList = new List(List.UNORDERED);
        actionList.setListSymbol("\u2022  ");
        actionList.setIndentationLeft(20);
        actionList.setIndentationRight(20);
        for (String action : results.getActions()) {
            ListItem item = new ListItem(action, bodyFont);
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            actionList.add(item);
        }
        document.add(actionList);
        Font bold = FontFactory.getFont("Helvetica", 10, Font.BOLD);
        if(results.getStage().equals("PREPARACIÓN") || results.getStage().equals("ACCIÓN") || results.getStage().equals("MANTENIMIENTO")){
            Paragraph conclusion = new Paragraph("Se recomienda que el paciente realice el cuestionario de aptitud para la actividad física para todos (PAR-Q+).", bold);
            conclusion.setSpacingBefore(30);
            document.add(conclusion);
        }

        Font footerFont = FontFactory.getFont("Helvetica", 10, Font.ITALIC);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Fecha y hora: " + LocalDateTime.now(ZoneId.of("Europe/Madrid")).format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", a las " + LocalTime.now(ZoneId.of("Europe/Madrid")).format(timeFormatter) , footerFont), 525, 45, 0);
        document.close();
        
        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_CMTCEF_" + cmtcef.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/cmtcef/", pdfMultipartFile);
            return returned.split(" ")[3];
        } catch (Exception e) {
            throw new IOException("Error uploading file to S3");
        }
    }

    public String generateParqPdf(Parq parq) throws DocumentException, IOException {
        Optional<Patient> optional = patientRepository.findById(parq.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();
        ParqResults results = ParqService.getResults(parq);
        Document document = new Document(PageSize.A4, 70, 70, 60, 30);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Cuestionario de aptitud para la actividad física para todos (PAR-Q+)", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());
        
        
        document.add(title);
        document.add(linebreak);
        
        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 695); // coordinates for the left column
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), parq.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p3 = new Paragraph("Fecha: " + parq.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + parq.getSession() + ")", leftColumnFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(3);
        p3.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p3);
        ct.go();
        
        ct.setSimpleColumn(340, 150, 500, 695); // coordinates for the right column
        Font rightColumnFont = FontFactory.getFont("Helvetica", 8, Font.ITALIC);
        Paragraph p5 = new Paragraph("Este informe no constituye un diagnóstico.\n" + //
                        "No olvide consultar a su médico antes de iniciar un programa de ejercicio físico.", rightColumnFont);
        p5.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        ct.addElement(p5);
        InputStream imgStream = new ClassPathResource("img/exercise4you.png").getInputStream();
        Image img = Image.getInstance(imgStream.readAllBytes());
        img.scaleToFit(80, 80); // adjust the size as needed
        img.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        img.setSpacingBefore(8);
        ct.addElement(img);
        ct.go();

        Paragraph emptyLine = new Paragraph("");
        emptyLine.setSpacingBefore(80);
        document.add(emptyLine);
        document.add(linebreak);

        Font bodyFont = FontFactory.getFont("Helvetica", 9);
        Font subtitleFont = FontFactory.getFont("Helvetica", 10, Font.BOLD);
        Paragraph recommendationsTitle = new Paragraph("RECOMENDACIONES", subtitleFont);
        recommendationsTitle.setSpacingBefore(10);
        recommendationsTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(recommendationsTitle);
        Paragraph recommendationReason = new Paragraph(results.getRecommendationReason(), bodyFont);
        recommendationReason.setSpacingBefore(8);
        recommendationReason.setLeading(12);
        recommendationReason.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        recommendationReason.setSpacingAfter(2);
        document.add(recommendationReason);

        List recommendations = new List(List.UNORDERED);
        recommendations.setListSymbol("\u2022  ");
        recommendations.setIndentationLeft(20);
        recommendations.setIndentationRight(20);
        for (String characteristic : results.getRecommendations()) {
            ListItem item = new ListItem(characteristic, bodyFont);
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            item.setLeading(12f);
            recommendations.add(item);
        }
        document.add(recommendations);

        if(!results.getIsAbleToExercise().booleanValue()){
            document.close();
            PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_PARQ_" + parq.getCompletionDate() +".pdf", buffer.toByteArray());
            try {
                String returned = s3Service.uploadMultipartFile("pdfs/parq/", pdfMultipartFile);
                return returned.split(" ")[3];
            } catch (Exception e) {
                throw new IOException("Error uploading file to S3");
        }
        }

        Paragraph delayExercise = new Paragraph("Retrase el inicio de su actividad física si:", bodyFont);
        delayExercise.setSpacingBefore(8);
        delayExercise.setLeading(12);
        delayExercise.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        delayExercise.setSpacingAfter(2);
        document.add(delayExercise);
        String[] delayReasons = {
            "No se siente bien debido a una enfermedad temporal como un resfriado o fiebre. Conviene esperar a que esté recuperado.",
            "Usted está embarazada: consulte con su médico, profesional de la salud cualificado en temas de ejercicio, y/o complete el ePARmed-X+ antes de empezar cualquier cambio en su actividad física habitual.",
            "Si su salud cambia, consulte con su médico u otro profesional de salud cualificado en temas de ejercicio antes de seguir con cualquier programa de actividad física."
        };

        List delay = new List(List.UNORDERED);
        delay.setListSymbol("\u2022  ");
        delay.setIndentationLeft(20);
        delay.setIndentationRight(20);
        for (String reason : delayReasons) {
            ListItem item = new ListItem(reason, bodyFont);
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            item.setLeading(12f);
            delay.add(item);
        }
        document.add(delay);

        Paragraph declarationTitle = new Paragraph("DECLARACIÓN DEL PACIENTE", subtitleFont);
        declarationTitle.setSpacingBefore(12);
        declarationTitle.setSpacingAfter(2);
        declarationTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(declarationTitle);
        String[] declarations = {
            "Todas las personas que hayan completado el PAR-Q+, por favor lean y firmen la declaración que se muestra a continuación.",
            "Si tiene menos de la edad legal requerida para dar consentimiento o necesita el consentimiento de la persona que presenta su tutela; su padre, tutor o cuidador también debe firmar este formulario.",
            "Yo, el abajo firmante, declara haber leído y comprendido el mencionado cuestionario. Reconozco que esta autorización de actividad física es válida por un máximo de 12 meses a partir de la fecha en la que se cumplimenta y deja de ser válida si mi condición cambia.",
            "Autorizo al centro de fisioterapia a guardar una copia de este cuestionario para uso interno. En cuyo caso la entidad estará obligada a respetar la confidencialidad de dicho documento, en cumplimiento de la ley en vigor."
        };
        for(String declaration : declarations){
            Paragraph p = new Paragraph(declaration, bodyFont);
            p.setSpacingBefore(6);
            p.setLeading(12f);
            p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            document.add(p);
        }


        ct.setSimpleColumn(90, 0, 310, 140); // coordinates for the left column
        p1 = new Paragraph("Nombre completo: " + patient.getName() + " " + patient.getSurnames(), bodyFont);
        p2 = new Paragraph("Firma: _____________", bodyFont);
        p3 = new Paragraph("Nombre del padre/tutor/cuidador:", bodyFont);
        Paragraph p4 = new Paragraph("Firma del padre/tutor/cuidador: _____________", bodyFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(12);
        p3.setSpacingAfter(3);
        p4.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p3);
        ct.addElement(p4);
        ct.go();

        ct.setSimpleColumn(330, 0, 500, 140); // coordinates for the right column
        p1 = new Paragraph("Nombre del testigo:", bodyFont);
        p2 = new Paragraph("Firma del testigo: _____________", bodyFont);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd, HH:mm");
        p3 = new Paragraph("Fecha y hora: " + dtf.format(ZonedDateTime.now(ZoneId.of("Europe/Madrid"))), bodyFont);
        p3.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(30);
        p3.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p3);
        ct.go();

        document.close();
        
        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_PARQ_" + parq.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/parq/", pdfMultipartFile);
            return returned.split(" ")[3];
        } catch (Exception e) {
            throw new IOException("Error uploading file to S3");
        }
    }

    public String generateEparmedPdf(Eparmed eparmed) throws DocumentException, IOException {
        Optional<Patient> optional = patientRepository.findById(eparmed.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();
        EparmedResults results = EparmedService.getResults(eparmed);
        Document document = new Document(PageSize.A4, 70, 70, 60, 30);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Informe sobre el Examen médico electrónico de aptitud para la actividad física (ePARmed-X+)", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());
        

        document.add(title);
        document.add(linebreak);

        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 695); // coordinates for the left column
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), eparmed.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p4 = new Paragraph("Fecha: " + eparmed.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + eparmed.getSession() + ")", leftColumnFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(3);
        p4.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p4);
        ct.go();

        ct.setSimpleColumn(340, 150, 500, 695); // coordinates for the right column
        Font rightColumnFont = FontFactory.getFont("Helvetica", 8, Font.ITALIC);
        Paragraph p5 = new Paragraph("Este informe no constituye un diagnóstico.\n" + //
                        "No olvide consultar a su médico antes de iniciar un programa de ejercicio físico.", rightColumnFont);
        p5.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        ct.addElement(p5);
        InputStream imgStream = new ClassPathResource("img/exercise4you.png").getInputStream();
        Image img = Image.getInstance(imgStream.readAllBytes());
        img.scaleToFit(80, 80); // adjust the size as needed
        img.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        img.setSpacingBefore(8);
        ct.addElement(img);
        ct.go();

        Paragraph emptyLine = new Paragraph("");
        emptyLine.setSpacingBefore(80);
        document.add(emptyLine);
        document.add(linebreak);

        Font bodyFont = FontFactory.getFont("Helvetica", 9);
        Font subtitleFont = FontFactory.getFont("Helvetica", 10, Font.BOLD);
        Paragraph recommendationsTitle = new Paragraph("RECOMENDACIONES", subtitleFont);
        recommendationsTitle.setSpacingBefore(10);
        recommendationsTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(recommendationsTitle);
        Paragraph recomendations = new Paragraph(results.getRecommendation(), bodyFont);
        recomendations.setSpacingBefore(8);
        recomendations.setLeading(12);
        recomendations.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(recomendations);

        Paragraph declarationTitle = new Paragraph("DECLARACIÓN DEL PACIENTE", subtitleFont);
        declarationTitle.setSpacingBefore(12);
        declarationTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(declarationTitle);
        Paragraph declaration = new Paragraph("Yo, el abajo firmante, declaro haber leído y comprendido el mencionado cuestionario. Estoy de acuerdo en que la presente declaración para realizar actividad física tiene una validez de " + results.getValidTime() + " meses a partir de la fecha en la que se completó el cuestionario y queda invalidada si hay cambios en mi estado de salud. Autorizo al centro de fisioterapia a guardar una copia de este cuestionario para uso interno. En cuyo caso la entidad estará obligada a respetar la confidencialidad de dicho documento, en cumplimiento de la ley en vigor.\n" + //
                        "\n" + //
                        "Los autores, la Colaboración PAR-Q+, las organizaciones asociadas y sus agentes no asumen ninguna responsabilidad por las personas que realizan actividad física. En caso de duda después de completar el cuestionario, consulte a su médico antes de realizar actividad física."
                        , bodyFont);
        declaration.setSpacingBefore(8);
        declaration.setLeading(12);
        declaration.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(declaration);


        ct.setSimpleColumn(90, 0, 310, 140); // coordinates for the left column
        p1 = new Paragraph("Nombre completo: " + patient.getName() + " "  + patient.getSurnames(), bodyFont);
        p2 = new Paragraph("Firma: _____________", bodyFont);
        Paragraph p3 = new Paragraph("Nombre del padre/tutor/cuidador:", bodyFont);
        p4 = new Paragraph("Firma del padre/tutor/cuidador: _____________", bodyFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(12);
        p3.setSpacingAfter(3);
        p4.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p3);
        ct.addElement(p4);
        ct.go();

        ct.setSimpleColumn(330, 0, 500, 140); // coordinates for the right column
        p1 = new Paragraph("Nombre del testigo:", bodyFont);
        p2 = new Paragraph("Firma del testigo: _____________", bodyFont);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd, HH:mm");
        p3 = new Paragraph("Fecha y hora: " + dtf.format(ZonedDateTime.now(ZoneId.of("Europe/Madrid"))), bodyFont);
        p3.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(30);
        p3.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p3);
        ct.go();

        document.close();
        
        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_EPARMED_" + eparmed.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/eparmed/", pdfMultipartFile);
            return returned.split(" ")[3];
        } catch (Exception e) {
            throw new IOException("Error uploading file to S3");
        }
    }

    public String generateApalqPdf(Apalq apalq) throws DocumentException, IOException {
        Optional<Patient> optional = patientRepository.findById(apalq.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();
        ApalqResults results = ApalqService.getResults(apalq, patient.getName(), patient.getSurnames());
        Document document = new Document(PageSize.A4, 70, 70, 60, 30);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Cuestionario de evaluación de los niveles de actividad física (APALQ)", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());

        document.add(title);
        document.add(linebreak);

        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 695); // coordinates for the left column
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), apalq.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p4 = new Paragraph("Fecha: " + apalq.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + apalq.getSession() + ")", leftColumnFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(3);
        p4.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p4);
        ct.go();

        ct.setSimpleColumn(340, 150, 500, 695); // coordinates for the right column
        InputStream imgStream = new ClassPathResource("img/exercise4you.png").getInputStream();
        Image img = Image.getInstance(imgStream.readAllBytes());
        img.scaleToFit(80, 80); // adjust the size as needed
        img.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        img.setSpacingBefore(10);
        ct.addElement(img);
        ct.go();
        
        Paragraph emptyLine = new Paragraph("");
        emptyLine.setSpacingBefore(55);
        document.add(emptyLine);
        document.add(linebreak);


        Font bodyFont = FontFactory.getFont("Helvetica", 9);
        Font boldBodyFont = FontFactory.getFont("Helvetica", 9, Font.BOLD);
        Font subtitleFont = FontFactory.getFont("Helvetica", 10, Font.BOLD);
        Paragraph introductionTitle = new Paragraph("INTRODUCCIÓN", subtitleFont);
        introductionTitle.setSpacingBefore(10);
        introductionTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(introductionTitle);
        Paragraph introduction = new Paragraph("El objetivo de este informe es presentar y analizar los resultados del Cuestionario APALQ aplicado a " + patient.getName() + " " + patient.getSurnames() + " para evaluar sus patrones de actividad física y determinar su nivel de actividad según los tramos establecidos.", bodyFont);
        introduction.setSpacingBefore(8);
        introduction.setLeading(12);
        introduction.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(introduction);

        Paragraph answersTitle = new Paragraph("RESULTADOS", subtitleFont);
        answersTitle.setSpacingBefore(12);
        answersTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(answersTitle);
        p1 = new Paragraph("Puntuación Global del Cuestionario:", boldBodyFont);
        p1.setSpacingBefore(8);
        p1.setLeading(12);
        p1.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p1);

        List answerList = new List(List.UNORDERED);
        answerList.setIndentationLeft(15);
        answerList.setIndentationRight(20);
        int i = 1;
        for (ApalqResults.Answer answer : results.getAnswers()) {
            ListItem item = new ListItem();
            item.add(new Paragraph("\u2022  Pregunta " + i + ": " + answer.getQuestion(), boldBodyFont));
            List subList = new List(List.UNORDERED);
            subList.setIndentationLeft(25);
            subList.setIndentationRight(20);
            ListItem subitem = new ListItem();
            subitem.add(new Paragraph("Respuesta: " + answer.getAnswer(), bodyFont));
            subList.add(subitem);
            subitem = new ListItem();
            subitem.add(new Paragraph("Valoración: " + answer.getScore(), bodyFont));
            subList.add(subitem);
            item.add(subList);
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            answerList.add(item);
        }
        document.add(answerList);

        p1 = new Paragraph("Puntuación Global del Cuestionario:", boldBodyFont);
        p1.setSpacingBefore(8);
        p1.setLeading(12);
        p1.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p1);
        List scoreList = new List(List.UNORDERED);
        scoreList.setListSymbol("  ");
        scoreList.setIndentationLeft(15);
        scoreList.setIndentationRight(20);
        ListItem item = new ListItem(new Paragraph("\u2022  \u2265 Puntuación total: " + results.getTotalScore(), bodyFont));
        scoreList.add(item);
        document.add(scoreList);

        p1 = new Paragraph("Interpretación de la Puntuación:", boldBodyFont);
        p1.setSpacingBefore(8);
        p1.setLeading(12);
        p1.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p1);
        scoreList = new List(List.UNORDERED);
        scoreList.setIndentationLeft(15);
        scoreList.setIndentationRight(20);
        item = new ListItem();
        item.add(new Paragraph("\u2022  Nivel de Actividad Física del Participante: ", bodyFont));
        List subList = new List(List.UNORDERED);
        subList.setIndentationLeft(25);
        subList.setIndentationRight(20);
        ListItem subitem = new ListItem();
        subitem.add(new Paragraph("Según la puntuación obtenida, " + patient.getName() + " " + patient.getSurnames() + " tiene un nivel " + results.getInterpretation() + " de actividad física.", bodyFont));
        subList.add(subitem);
        item.add(subList);
        scoreList.add(item);
        document.add(scoreList);

        Paragraph analysisTitle = new Paragraph("ANÁLISIS DE RESULTADOS", subtitleFont);
        analysisTitle.setSpacingBefore(10);
        analysisTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(analysisTitle);
        Paragraph analysis = new Paragraph(results.getAnalysis(), bodyFont);
        analysis.setSpacingBefore(8);
        analysis.setLeading(12);
        analysis.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(analysis);

        document.newPage();

        Paragraph recommendationTitle = new Paragraph("RECOMENDACIONES", subtitleFont);
        recommendationTitle.setSpacingBefore(10);
        recommendationTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(recommendationTitle);
        Paragraph recommendation = new Paragraph(results.getRecommendation(), bodyFont);
        recommendation.setSpacingBefore(8);
        recommendation.setLeading(12);
        recommendation.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(recommendation);

        Paragraph conclusionTitle = new Paragraph("CONCLUSIÓN", subtitleFont);
        conclusionTitle.setSpacingBefore(10);
        conclusionTitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(conclusionTitle);
        Paragraph conclusion = new Paragraph(results.getConclusion(), bodyFont);
        conclusion.setSpacingBefore(8);
        conclusion.setLeading(12);
        conclusion.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(conclusion);

        Font footerFont = FontFactory.getFont("Helvetica", 10, Font.ITALIC);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Fecha y hora: " + LocalDateTime.now(ZoneId.of("Europe/Madrid")).format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", a las " + LocalTime.now(ZoneId.of("Europe/Madrid")).format(timeFormatter) , footerFont), 525, 45, 0);
        document.close();
        
        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_APALQ_" + apalq.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/apalq/", pdfMultipartFile);
            return returned.split(" ")[3];
        } catch (Exception e) {
            throw new IOException("Error uploading file to S3");
        }
    }

    public String generateObjectivesPdf(ObjectivesResponse objectivesResponse) throws DocumentException, MalformedURLException, IOException {
        Optional<Patient> optional = patientRepository.findById(objectivesResponse.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();

        Document document = new Document(PageSize.A4, 70, 70, 60, 60);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Objetivos", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());
        

        document.add(title);
        document.add(linebreak);

        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 725); // coordinates for the left column
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), objectivesResponse.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p4 = new Paragraph("Fecha: " + objectivesResponse.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + objectivesResponse.getSession() + ")", leftColumnFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(3);
        p4.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p4);
        ct.go();

        ct.setSimpleColumn(340, 150, 500, 725); // coordinates for the right column
        InputStream imgStream = new ClassPathResource("img/exercise4you.png").getInputStream();
        Image img = Image.getInstance(imgStream.readAllBytes());
        img.scaleToFit(80, 80); // adjust the size as needed
        img.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        img.setSpacingBefore(8);
        ct.addElement(img);
        ct.go();

        Paragraph emptyLine = new Paragraph("");
        emptyLine.setSpacingBefore(55);
        document.add(emptyLine);
        document.add(linebreak);

        Font subtitleFont = FontFactory.getFont("Helvetica", 14, Font.BOLD);
        Paragraph subtitle = new Paragraph("Descripción", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(5);
        subtitle.setSpacingAfter(5);
        document.add(subtitle);

        Font bodyFont = FontFactory.getFont("Helvetica", 10);
        Font boldBodyFont = FontFactory.getFont("Helvetica", 10, Font.BOLD);
        Paragraph p = new Paragraph();
        p.setSpacingBefore(10);
        p.setSpacingAfter(10);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("Partiendo de los objetivos generales previamente definidos para el paciente y contando con su opinión y participación, se han establecido objetivos específicos durante esta sesión con el fin de guiar de manera efectiva su programa de ejercicio terapéutico. La colaboración del paciente ha sido fundamental para asegurar que los objetivos reflejen sus necesidades, preferencias y capacidades. Estos objetivos se han formulado utilizando la metodología SMART, que asegura que cada objetivo cumpla con los criterios de ser ", bodyFont));
        p.add(new Chunk("Específico", boldBodyFont));
        p.add(new Chunk(", ", bodyFont));
        p.add(new Chunk("Medible", boldBodyFont));
        p.add(new Chunk(", ", bodyFont));
        p.add(new Chunk("Alcanzable", boldBodyFont));
        p.add(new Chunk(", ", bodyFont));
        p.add(new Chunk("Relevante", boldBodyFont));
        p.add(new Chunk(" y con un ", bodyFont));
        p.add(new Chunk("Tiempo definido", boldBodyFont));
        p.add(new Chunk(".", bodyFont));
        document.add(p);

        String[] boldTexts = {"Específicos:", "Medibles:", "Alcanzables:", "Relevantes:", "Tiempo definido:"};
        String[] definitions = {" Cada objetivo detalla claramente lo que se busca lograr. Por ejemplo, mejorar la fuerza muscular en una área determinada del cuerpo o incrementar la capacidad cardiovascular del paciente.",
                        " Se han establecido indicadores cuantificables que permitirán evaluar el progreso de manera objetiva. Esto incluye el uso de pruebas estandarizadas, como el test 6 minutos marcha o cuestionarios de calidad de vida, que facilitan la monitorización de los avances alcanzados.",
                        " Los objetivos han sido ajustados a las capacidades actuales del paciente, garantizando que sean realistas y factibles de lograr dentro de sus limitaciones físicas y contextuales. Esto incluye un equilibrio entre el desafío y la seguridad para promover una progresión constante.",
                        " Cada objetivo está alineado con las necesidades y prioridades del paciente, asegurando que las intervenciones tengan un impacto significativo en su bienestar general y en la mejora de su calidad de vida.",
                        " Se ha establecido un marco temporal claro para la consecución de cada objetivo, lo que facilita la planificación de las actividades y permite realizar evaluaciones periódicas para ajustar el programa según sea necesario."};
        List list = new List(List.UNORDERED);
        list.setListSymbol("\u2022  ");
        list.setIndentationLeft(20);
        list.setIndentationRight(20);
        for (int i = 0; i < boldTexts.length; i++) {
            ListItem item = new ListItem();
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            item.add(new Chunk(boldTexts[i], boldBodyFont));
            item.add(new Chunk(definitions[i], bodyFont));
            item.setLeading(0, 1);
            list.add(item);
        }
        document.add(list);

        p = new Paragraph("La aplicación de la metodología SMART en la definición de los objetivos garantiza una intervención personalizada y estructurada, promoviendo la motivación y el compromiso del paciente. Además, facilita la evaluación continua del progreso, permitiendo realizar los ajustes necesarios para optimizar los resultados del programa de ejercicio terapéutico.", bodyFont);
        p.setSpacingBefore(25);
        p.setSpacingAfter(15);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p);

        subtitle = new Paragraph("Características del paciente", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);
        
        list = new List(List.UNORDERED);
        list.setListSymbol("\u2022  ");
        list.setIndentationLeft(20);
        list.setIndentationRight(20);
        ListItem item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        item.add(new Chunk("Grupo poblacional: ", boldBodyFont));
        item.add(new Chunk(objectivesResponse.getObjectives().get(0).getPopulationGroup(), bodyFont));
        list.add(item);
        item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        item.add(new Chunk("Padece enfermedades crónicas: ", boldBodyFont));
        item.add(new Chunk(objectivesResponse.getObjectives().get(0).getChronicDisease(), bodyFont));
        list.add(item);
        item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        item.add(new Chunk("Grupo de enfermedades crónicas: ", boldBodyFont));
        item.add(new Chunk(objectivesResponse.getObjectives().get(0).getGroupOfChronicDiseases(), bodyFont));
        list.add(item);
        item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        item.add(new Chunk("Enfermedad que padece: ", boldBodyFont));
        item.add(new Chunk(objectivesResponse.getObjectives().get(0).getDisease(), bodyFont));
        list.add(item);
        document.add(list);
        
        p = new Paragraph("A continuación, se detallan los diferentes objetivos acordados con el paciente:", bodyFont);
        p.setSpacingAfter(15);
        p.setSpacingBefore(15);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p);

        for(Objective objective : objectivesResponse.getObjectives()) {
            document.newPage();

            subtitle = new Paragraph("Objetivo " + (objectivesResponse.getObjectives().indexOf(objective)+1), subtitleFont);
            subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(5);
            subtitle.setSpacingAfter(5);
            document.add(subtitle);

            addElementName(document, "Objetivo: ");
            addElementDetails(document, objective.getObjective());
            addElementName(document, "Rango: ");
            addElementDetails(document, objective.getRange());
            addElementName(document, "Prueba o cuestionario: ");
            addElementDetails(document, objective.getTestOrQuestionnaire());
            addElementName(document, "Específico: ");
            addElementDetails(document, objective.getSpecific());
            addElementName(document, "Medible: ");
            addElementDetails(document, objective.getMeasurable());
            addElementName(document, "Alcanzable: ");
            addElementDetails(document, objective.getAchievable());
            addElementName(document, "Relevante: ");
            addElementDetails(document, objective.getRelevant());
            addElementName(document, "Temporal: ");
            addElementDetails(document, objective.getTemporal());
            addElementName(document, "Objetivo SMART: ");
            addElementDetails(document, objective.getSmartObjective());
        }

        Font footerFont = FontFactory.getFont("Helvetica", 10, Font.ITALIC);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Fecha y hora: " + LocalDateTime.now(ZoneId.of("Europe/Madrid")).format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", a las " + LocalTime.now(ZoneId.of("Europe/Madrid")).format(timeFormatter) , footerFont), 525, 45, 0);
        document.close();

        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_OBJECTIVES_" + objectivesResponse.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/objectives/", pdfMultipartFile);
            return returned.split(" ")[3];
        } catch (Exception e) {
            throw new IOException("Error uploading file to S3");
        }
    }

    public String generatePrescriptionsPdf(PrescriptionsResponse prescriptionsResponse) throws DocumentException, MalformedURLException, IOException {

        Optional<Patient> optional = patientRepository.findById(prescriptionsResponse.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();

        Document document = new Document(PageSize.A4, 70, 70, 60, 60);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Prescripción de ejercicio físico", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());
        

        document.add(title);
        document.add(linebreak);

        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 725); // coordinates for the left column
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), prescriptionsResponse.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p4 = new Paragraph("Fecha: " + prescriptionsResponse.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + prescriptionsResponse.getSession() + ")", leftColumnFont);
        p1.setSpacingAfter(3);
        p2.setSpacingAfter(3);
        p4.setSpacingAfter(3);
        ct.addElement(p1);
        ct.addElement(p2);
        ct.addElement(p4);
        ct.go();

        ct.setSimpleColumn(340, 150, 500, 725); // coordinates for the right column
        Font rightColumnFont = FontFactory.getFont("Helvetica", 8, Font.ITALIC);
        Paragraph p5 = new Paragraph("Este informe no constituye un diagnóstico.\n" + //
                        "No olvide consultar a su médico antes de iniciar un programa de ejercicio físico.", rightColumnFont);
        p5.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        ct.addElement(p5);
        InputStream imgStream = new ClassPathResource("img/exercise4you.png").getInputStream();
        Image img = Image.getInstance(imgStream.readAllBytes());
        img.scaleToFit(80, 80); // adjust the size as needed
        img.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        img.setSpacingBefore(8);
        ct.addElement(img);
        ct.go();

        Paragraph emptyLine = new Paragraph("");
        emptyLine.setSpacingBefore(80);
        document.add(emptyLine);
        document.add(linebreak);

        Font subtitleFont = FontFactory.getFont("Helvetica", 14, Font.BOLD);
        Paragraph subtitle = new Paragraph("Descripción", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(5);
        subtitle.setSpacingAfter(5);
        document.add(subtitle);

        Font bodyFont = FontFactory.getFont("Helvetica", 10);
        Font boldBodyFont = FontFactory.getFont("Helvetica", 10, Font.BOLD);
        Paragraph p = new Paragraph("A continuación, le presentamos una descripción general de la prescripción de ejercicio diseñada específicamente para usted. Este plan se basa en sus necesidades y objetivos, con la finalidad de mejorar su estado de salud y prevenir posibles complicaciones asociadas a su condición. Cada una de las recomendaciones aquí incluidas ha sido cuidadosamente seleccionada en función de la información que usted nos ha proporcionado y de las guías clínicas más actualizadas.", bodyFont);
        p.setSpacingBefore(10);
        p.setSpacingAfter(10);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p);

        p = new Paragraph("Nuestro enfoque parte de la identificación de su población objetivo (edad y características fisiológicas) y de las enfermedades crónicas o factores de riesgo que pudieran requerir un seguimiento especial. A partir de estos datos, se han determinado tanto el nivel de exigencia que mejor se adapta a su estado actual como el tipo de ejercicio (aeróbico, fortalecimiento muscular, fortalecimiento muscular y óseo o neuromuscular) y la modalidad más adecuada (continuo, dinámico, estático, balístico, fuerza dinámica, fuerza isométrica, etc.).", bodyFont);
        p.setSpacingAfter(10);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p);

        p = new Paragraph("La prescripción incluye indicaciones sobre la frecuencia, la intensidad, la duración, el tipo de entrenamiento y el volumen total de trabajo recomendado a la semana. Además, se ha planificado una progresión gradual para que su organismo se adapte al ejercicio sin riesgos innecesarios. Esta planificación tiene siempre en cuenta los objetivos específicos establecidos para usted, como podrían ser el aumento de la fuerza, la mejora de la capacidad aeróbica o la reducción del dolor articular.", bodyFont);
        p.setSpacingAfter(10);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p);

        p = new Paragraph("Por último, se han incorporado consideraciones especiales que recogen pautas de seguridad, posibles contraindicaciones, necesidad de supervisión y otros factores relevantes para su caso en particular. Estas precauciones aseguran que el programa sea eficaz y seguro, teniendo en cuenta cualquier limitación o requerimiento específico.", bodyFont);
        p.setSpacingAfter(10);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p);

        p = new Paragraph("A continuación, podrá consultar la prescripción de ejercicio generada, que detalla los elementos mencionados de manera individualizada. Este plan está pensado para promover su salud de forma sostenible, favoreciendo la adherencia y permitiendo realizar ajustes según sus avances y necesidades. Si tiene cualquier duda, no dude en contactar con su profesional de referencia para resolverla.", bodyFont);
        p.setSpacingAfter(10);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p);

        subtitle = new Paragraph("Características del paciente", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);
        
        List list = new List(List.UNORDERED);
        list.setListSymbol("\u2022  ");
        list.setIndentationLeft(20);
        list.setIndentationRight(20);
        ListItem item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        item.add(new Chunk("Grupo poblacional: ", boldBodyFont));
        item.add(new Chunk(prescriptionsResponse.getPrescriptions().get(0).getPopulationGroup(), bodyFont));
        list.add(item);
        item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        item.add(new Chunk("Padece enfermedades crónicas: ", boldBodyFont));
        item.add(new Chunk(prescriptionsResponse.getPrescriptions().get(0).getChronicDisease(), bodyFont));
        list.add(item);
        item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        item.add(new Chunk("Grupo de enfermedades crónicas: ", boldBodyFont));
        item.add(new Chunk(prescriptionsResponse.getPrescriptions().get(0).getGroupOfChronicDiseases(), bodyFont));
        list.add(item);
        item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        item.add(new Chunk("Enfermedad que padece: ", boldBodyFont));
        item.add(new Chunk(prescriptionsResponse.getPrescriptions().get(0).getDisease(), bodyFont));
        list.add(item);
        document.add(list);

        for(Prescription prescription : prescriptionsResponse.getPrescriptions()) {
            document.newPage();

            subtitle = new Paragraph("Ejercicio " + (prescriptionsResponse.getPrescriptions().indexOf(prescription)+1), subtitleFont);
            subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(5);
            subtitle.setSpacingAfter(5);
            document.add(subtitle);

            addPrescriptionElement(document, "Ejercicio", prescription.getExercise());
            addPrescriptionElement(document, "Modalidad", prescription.getModality());
            addPrescriptionElement(document, "Frecuencia", prescription.getFrequency());
            addPrescriptionElement(document, "Intensidad", prescription.getIntensity());
            addPrescriptionElement(document, "Tiempo", prescription.getTime());
            addPrescriptionElement(document, "Tipo", prescription.getType());
            addPrescriptionElement(document, "Volumen", prescription.getVolume());
            addPrescriptionElement(document, "Progresión", prescription.getProgression());

        }

        if(prescriptionsResponse.getPrescriptions().get(0).getSpecialConsiderations() != null && !prescriptionsResponse.getPrescriptions().get(0).getSpecialConsiderations().isEmpty()) {
            document.newPage();

            subtitle = new Paragraph("Consideraciones especiales", subtitleFont);
            subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(5);
            subtitle.setSpacingAfter(5);
            document.add(subtitle);

            String[] lines = prescriptionsResponse.getPrescriptions().get(0).getSpecialConsiderations().split("\\r?\\n");
            emptyLine = new Paragraph("");
            emptyLine.setSpacingBefore(5);
            document.add(emptyLine);
            list = new List(List.UNORDERED);
            list.setListSymbol("\u2022  ");
            list.setIndentationLeft(20);
            list.setIndentationRight(20);
            
            for(String line : lines) {
                item = new ListItem();
                item.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
                item.add(new Chunk(line, bodyFont));
                list.add(item);
            }
            document.add(list); 
        }

        Font footerFont = FontFactory.getFont("Helvetica", 10, Font.ITALIC);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Fecha y hora: " + LocalDateTime.now(ZoneId.of("Europe/Madrid")).format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", a las " + LocalTime.now(ZoneId.of("Europe/Madrid")).format(timeFormatter) , footerFont), 525, 45, 0);
        document.close();

        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_PRESCRIPTION_" + prescriptionsResponse.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/prescription/", pdfMultipartFile);
            return returned.split(" ")[3];
        } catch (Exception e) {
            throw new IOException("Error uploading file to S3");
        }
    }

    private static void addPrescriptionElement(Document document, String name, String details) throws DocumentException {
        if(details == null || details.isEmpty()) {
            return;
        }
        addPrescriptionElementName(document, name);
        addPrescriptionElementDetails(document, details);
    }

    private static void addPrescriptionElementName(Document document, String name) throws DocumentException {
        Font boldBodyFont = FontFactory.getFont("Helvetica", 10, Font.BOLD);
        List list = new List(List.UNORDERED);
        list.setListSymbol("\u2022  ");
        list.setIndentationLeft(20);
        list.setIndentationRight(20);
        ListItem item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        item.add(new Chunk(name + ": ", boldBodyFont));
        list.add(item);
        document.add(list);
    }

    private static void addPrescriptionElementDetails(Document document, String details) throws DocumentException {
        Font bodyFont = FontFactory.getFont("Helvetica", 10);
        String[] lines = details.split("\\r?\\n");
        Paragraph emptyLine = new Paragraph("");
        emptyLine.setSpacingBefore(5);
        document.add(emptyLine);
        for(String line : lines) {
            Paragraph p = new Paragraph(line, bodyFont);
            p.setIndentationLeft(40);
            p.setSpacingAfter(5);
            p.setLeading(12);
            p.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
            document.add(p);
        }
        document.add(emptyLine);
        
    }

    private static void addElementName(Document document, String name) throws DocumentException {
        Font boldBodyFont = FontFactory.getFont("Helvetica", 10, Font.BOLD);
        List list = new List(List.UNORDERED);
        list.setListSymbol("\u2022  ");
        list.setIndentationLeft(20);
        list.setIndentationRight(20);
        ListItem item = new ListItem();
        item.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
        item.add(new Chunk(name, boldBodyFont));
        list.add(item);
        document.add(list);
    }

    private static void addElementDetails(Document document, String details) throws DocumentException {
        Font bodyFont = FontFactory.getFont("Helvetica", 10);
        Paragraph p = new Paragraph(details, bodyFont);
        p.setIndentationLeft(40);
        p.setSpacingAfter(10);
        p.setSpacingBefore(5);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
        document.add(p);
    }

    private static int getYearsBetween(LocalDate date1, LocalDate date2) {
        return Period.between(date1, date2).getYears();
    }
}
