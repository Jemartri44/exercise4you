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
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.PdfMultipartFile;
import es.codeurjc.exercise4you.entity.objectives.Objective;
import es.codeurjc.exercise4you.entity.objectives.ObjectivesResponse;
import es.codeurjc.exercise4you.entity.prescriptions.Prescription;
import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionsResponse;
import es.codeurjc.exercise4you.entity.questionnaire.*;
import es.codeurjc.exercise4you.entity.questionnaire.results.*;
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
        Document document = new Document(PageSize.A4, 70, 70, 60, 80);
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
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 50, 0);
        ct.setSimpleColumn(70, 30, 525, 85); // coordinates for the right column
        linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
        ct.addElement(linebreak);
        ct.go();

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

        float yPos = writer.getVerticalPosition(true);

        if(yPos < 183){
            document.newPage();
        }

        ct.setSimpleColumn(90, 0, 310, 160); // coordinates for the left column
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

        ct.setSimpleColumn(330, 0, 500, 160); // coordinates for the right column
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

        if(writer.getPageNumber() == 2){
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 50, 0);
            ct.setSimpleColumn(70, 30, 525, 85); // coordinates for the right column
            linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
            ct.addElement(linebreak);
            ct.go();
        }

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

    public String generateSfPdf(Sf sf) throws DocumentException, MalformedURLException, IOException {
        Optional<Patient> optional = patientRepository.findById(sf.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();
        SfResults results = SfService.getResults(sf);

        Document document = new Document(PageSize.A4, 70, 70, 60, 60);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Cuestionario de salud SF-36", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());

        document.add(title);
        document.add(linebreak);

        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 725); // coordinates for the left column
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), sf.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p4 = new Paragraph("Fecha: " + sf.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + sf.getSession() + ")", leftColumnFont);
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
        img.scaleToFit(100,100);

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
        p.setSpacingAfter(5);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("El SF-36 (del inglés Medical Outcomes Study 36-Item Short-Form Health Survey) es un cuestionario ampliamente utilizado para evaluar la ", bodyFont));
        p.add(new Chunk("calidad de vida relacionada con la salud", boldBodyFont));
        p.add(new Chunk(" en diversos contextos clínicos y de investigación. Se compone de ", bodyFont));
        p.add(new Chunk("36 ítems", boldBodyFont));
        p.add(new Chunk(" que abarcan ocho dimensiones o escalas principales:", bodyFont));
        document.add(p);

        String[] boldTexts = {"Función física", "Rol físico", "Dolor corporal", "Salud general", "Vitalidad", "Función social", "Rol emocional", "Salud mental"};
        String[] definitions = {"", " (limitaciones en el desempeño de actividades cotidianas por problemas físicos)", "", "", " (energía y fatiga)", "", " (limitaciones en el desempeño de actividades cotidianas por problemas emocionales)", ""};
        List list = new List(List.ORDERED);
        list.setIndentationLeft(20);
        list.setIndentationRight(20);
        for (int i = 0; i < boldTexts.length; i++) {
            ListItem item = new ListItem();
            item.setFont(boldBodyFont);
            item.setSpacingAfter(i == boldTexts.length - 1 ? 0 : 2);
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            item.add(new Chunk(boldTexts[i], boldBodyFont));
            item.add(new Chunk(definitions[i], bodyFont));
            item.setLeading(0, 1.2f);
            list.add(item);
        }
        document.add(list);

        p = new Paragraph();
        p.setSpacingBefore(5);
        p.setSpacingAfter(10);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("Estas ocho dimensiones, en conjunto, permiten obtener un ", bodyFont));
        p.add(new Chunk("perfil amplio", boldBodyFont));
        p.add(new Chunk(" de la ", bodyFont));
        p.add(new Chunk("salud percibida", boldBodyFont));
        p.add(new Chunk(" por la persona, tanto desde la perspectiva física como mental.", bodyFont));
        document.add(p);

        subtitle = new Paragraph("Características y utilidad clínica", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        boldTexts = new String[]{"Aplicabilidad: ", "Validez y fiabilidad: ", "Comparaciones poblacionales: "};
        definitions = new String[]{"El SF-36 está diseñado para ser auto-administrado, administrado por teléfono o aplicado mediante entrevista presencial a partir de los 14 años de edad.",
            "Ha sido validado en numerosas poblaciones y enfermedades crónicas, mostrando buena reproducibilidad y sensibilidad a los cambios en el estado de salud de los pacientes.",
            "Al contar con valores normativos para distintas poblaciones, el SF-36 posibilita la comparación de los resultados de un individuo o grupo con patrones de referencia."};
        
        list = new List(List.UNORDERED);
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

        subtitle = new Paragraph("Relevancia en ejercicio terapéutico", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(10);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);
        
        p = new Paragraph();
        p.setSpacingAfter(5);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("El ", bodyFont));
        p.add(new Chunk("SF-36", boldBodyFont));
        p.add(new Chunk(" suele emplearse en programas de rehabilitación y ejercicio terapéutico para evaluar la evolución del paciente no solo en términos de mejoría física, sino también en ", bodyFont));
        p.add(new Chunk("bienestar psicológico", boldBodyFont));
        p.add(new Chunk(" y ", bodyFont));
        p.add(new Chunk("social", boldBodyFont));
        p.add(new Chunk(". En el contexto de la presente aplicación, resulta de gran utilidad para:", bodyFont));
        document.add(p);

        boldTexts = new String[]{"Monitorizar", "Personalizar", "Evaluar"};
        definitions = new String[]{" la efectividad de las intervenciones de ejercicio en la calidad de vida.",
            " los programas de actividad física, ajustando la intensidad y frecuencia de los ejercicios a la respuesta individual de los usuarios.",
            " el impacto global de la adherencia al ejercicio, valorando no solo la sintomatología física, sino también la percepción subjetiva de salud."};
        list = new List(List.UNORDERED);
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

        Font footerFont = FontFactory.getFont("Helvetica", 10, Font.ITALIC);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 75, 0);
        ct.setSimpleColumn(70, 30, 525, 110); // coordinates for the right column
        linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
        ct.addElement(linebreak);
        ct.go();

        document.newPage();

        subtitle = new Paragraph("Conclusión y aplicación en", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(subtitle);

        subtitle = new Paragraph("la evaluación de la calidad de vida", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        p = new Paragraph();
        p.setSpacingAfter(5);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("Al incorporar el ", bodyFont));
        p.add(new Chunk("SF-36", boldBodyFont));
        p.add(new Chunk(" en la evaluación de los pacientes que participan en programas de ejercicio terapéutico, se obtiene una medida estandarizada de cómo perciben su salud y bienestar en ámbitos físicos, emocionales y sociales. Este enfoque integral resulta esencial para:", bodyFont));
        document.add(p);

        definitions = new String[]{"Detectar tempranamente posibles dificultades asociadas a la enfermedad o condición física, permitiendo ajustes oportunos en el programa de intervención.",
            "Evaluar la evolución del paciente a lo largo del proceso de rehabilitación, identificando cambios en la calidad de vida que reflejen la eficacia de las intervenciones terapéuticas.",
            "Brindar un soporte integral que considere tanto aspectos físicos como psicológicos, contribuyendo a la optimización del plan de rehabilitación y a la adherencia a largo plazo."};
        list = new List(List.ORDERED);
        list.setIndentationLeft(20);
        list.setIndentationRight(20);
        for (int i = 0; i < boldTexts.length; i++) {
            ListItem item = new ListItem();
            item.setFont(boldBodyFont);
            item.setSpacingAfter(i == boldTexts.length - 1 ? 0 : 2);
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            item.add(new Chunk(definitions[i], bodyFont));
            item.setLeading(0, 1.2f);
            list.add(item);
        }
        document.add(list);

        p = new Paragraph();
        p.setSpacingBefore(5);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("Gracias a la aplicación sistemática del ", bodyFont));
        p.add(new Chunk("SF-36", boldBodyFont));
        p.add(new Chunk(", la atención clínica se orienta más allá de los síntomas físicos y se enfoca en la ", bodyFont));
        p.add(new Chunk("experiencia global de salud", boldBodyFont));
        p.add(new Chunk(", apoyando la ", bodyFont));
        p.add(new Chunk("toma de decisiones terapéuticas", boldBodyFont));
        p.add(new Chunk(" basadas en la evidencia y la ", bodyFont));
        p.add(new Chunk("personalización", boldBodyFont));
        p.add(new Chunk(" de las intervenciones.", bodyFont));
        document.add(p);

        subtitle = new Paragraph("Resultados", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(10);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        // Crear una tabla de 3 columnas
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100); // Ocupa el 100% del ancho disponible
        // Definir anchos relativos de las columnas (por ejemplo, 2:1:5)
        float[] columnWidths = {2.8f, 1.8f, 7f};
        table.setWidths(columnWidths);

        // Definir un padding (espaciado interno) para las celdas
        int cellPadding = 6;
        float cellPaddingTopRatio = 0.5f;

        // Definir el color de la cabecera
        BaseColor headerColor = new BaseColor(98, 151, 208, 100);

        Font resultsFont = FontFactory.getFont("Helvetica", 12, Font.BOLD, new BaseColor(13, 62, 160));

        // --- Fila de cabecera ---
        PdfPCell cell;

        cell = new PdfPCell(new Paragraph("Dimensiones", boldBodyFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(cellPadding);
        cell.setPaddingTop(cellPadding * cellPaddingTopRatio);
        cell.setBackgroundColor(headerColor);
        table.addCell(cell);

        cell = new PdfPCell(new Paragraph("Puntuación sobre 100", boldBodyFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(cellPadding);
        cell.setPaddingTop(cellPadding * cellPaddingTopRatio);
        cell.setBackgroundColor(headerColor);
        table.addCell(cell);

        cell = new PdfPCell(new Paragraph("Resumen del contenido", boldBodyFont));
        // Para la cabecera se puede centrar también o alinear de otra forma
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(cellPadding);
        cell.setPaddingTop(cellPadding * cellPaddingTopRatio);
        cell.setBackgroundColor(headerColor);
        table.addCell(cell);

        // --- Fila 1: Función física ---
        PdfPCell cell1 = new PdfPCell(new Paragraph("Función física", bodyFont));
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell1.setPadding(cellPadding);
        cell1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Paragraph(String.valueOf(results.getPhysicalFunction()), resultsFont));
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell2.setPadding(cellPadding);
        cell2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(cell2);

        PdfPCell cell3 = new PdfPCell(new Paragraph(
                "Grado en que la salud limita las actividades físicas tales como el autocuidado, caminar, subir escaleras, inclinarse, " +
                "coger o llevar pesos, y los esfuerzos moderados e intensos.", bodyFont));
        cell3.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell3.setPadding(cellPadding);
        cell3.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(cell3);

        // --- Fila 2: Rol físico ---
        PdfPCell r2c1 = new PdfPCell(new Paragraph("Rol físico", bodyFont));
        r2c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r2c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r2c1.setPadding(cellPadding);
        r2c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r2c1);

        PdfPCell r2c2 = new PdfPCell(new Paragraph(String.valueOf(results.getRolePhysical()), resultsFont));
        r2c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r2c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r2c2.setPadding(cellPadding);
        r2c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r2c2);

        PdfPCell r2c3 = new PdfPCell(new Paragraph(
                "Grado en que la salud física interfiere en el trabajo y en otras actividades diarias, lo que incluye el rendimiento " +
                "menor que el deseado, la limitación en el tipo de actividades realizadas o la dificultad en la realización de actividades.", bodyFont));
        r2c3.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        r2c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r2c3.setPadding(cellPadding);
        r2c3.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r2c3);

        // --- Fila 3: Dolor corporal ---
        PdfPCell r3c1 = new PdfPCell(new Paragraph("Dolor corporal", bodyFont));
        r3c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r3c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r3c1.setPadding(cellPadding);
        r3c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r3c1);

        PdfPCell r3c2 = new PdfPCell(new Paragraph(String.valueOf(results.getBodilyPain()), resultsFont));
        r3c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r3c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r3c2.setPadding(cellPadding);
        r3c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r3c2);

        PdfPCell r3c3 = new PdfPCell(new Paragraph(
                "La intensidad del dolor y su efecto en el trabajo habitual, tanto fuera de casa como en el hogar.", bodyFont));
        r3c3.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        r3c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r3c3.setPadding(cellPadding);
        r3c3.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r3c3);

        // --- Fila 4: Salud general ---
        PdfPCell r4c1 = new PdfPCell(new Paragraph("Salud general", bodyFont));
        r4c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r4c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r4c1.setPadding(cellPadding);
        r4c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r4c1);

        PdfPCell r4c2 = new PdfPCell(new Paragraph(String.valueOf(results.getGeneralHealth()), resultsFont));
        r4c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r4c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r4c2.setPadding(cellPadding);
        r4c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r4c2);

        PdfPCell r4c3 = new PdfPCell(new Paragraph(
                "Valoración personal de la salud que incluye la salud actual, las perspectivas de salud en el futuro y la resistencia a enfermar.", bodyFont));
        r4c3.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        r4c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r4c3.setPadding(cellPadding);
        r4c3.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r4c3);

        // --- Fila 5: Vitalidad ---
        PdfPCell r5c1 = new PdfPCell(new Paragraph("Vitalidad", bodyFont));
        r5c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r5c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r5c1.setPadding(cellPadding);
        r5c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r5c1);

        PdfPCell r5c2 = new PdfPCell(new Paragraph(String.valueOf(results.getVitality()), resultsFont));
        r5c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r5c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r5c2.setPadding(cellPadding);
        r5c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r5c2);

        PdfPCell r5c3 = new PdfPCell(new Paragraph(
                "Sentimiento de energía y vitalidad, frente al sentimiento de cansancio y agotamiento.", bodyFont));
        r5c3.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        r5c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r5c3.setPadding(cellPadding);
        r5c3.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r5c3);

        // --- Fila 6: Función social ---
        PdfPCell r6c1 = new PdfPCell(new Paragraph("Función social", bodyFont));
        r6c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r6c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r6c1.setPadding(cellPadding);
        r6c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r6c1);

        PdfPCell r6c2 = new PdfPCell(new Paragraph(String.valueOf(results.getSocialFunction()), resultsFont));
        r6c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r6c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r6c2.setPadding(cellPadding);
        r6c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r6c2);

        PdfPCell r6c3 = new PdfPCell(new Paragraph(
                "Grado en el que los problemas de salud física o emocional interfieren en la vida social habitual.", bodyFont));
        r6c3.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        r6c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r6c3.setPadding(cellPadding);
        r6c3.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r6c3);

        // --- Fila 7: Rol emocional ---
        PdfPCell r7c1 = new PdfPCell(new Paragraph("Rol emocional", bodyFont));
        r7c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r7c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r7c1.setPadding(cellPadding);
        r7c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r7c1);

        PdfPCell r7c2 = new PdfPCell(new Paragraph(String.valueOf(results.getRoleEmotional()), resultsFont));
        r7c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r7c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r7c2.setPadding(cellPadding);
        r7c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r7c2);

        PdfPCell r7c3 = new PdfPCell(new Paragraph(
                "Grado en el que los problemas emocionales interfieren en el trabajo u otras actividades diarias, lo que incluye la reducción en el tiempo " +
                "dedicado a esas actividades, el rendimiento menor que el deseado y una disminución del cuidado al trabajar.", bodyFont));
        r7c3.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        r7c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r7c3.setPadding(cellPadding);
        r7c3.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r7c3);

        // --- Fila 8: Salud mental ---
        PdfPCell r8c1 = new PdfPCell(new Paragraph("Salud mental", bodyFont));
        r8c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r8c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r8c1.setPadding(cellPadding);
        r8c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r8c1);

        PdfPCell r8c2 = new PdfPCell(new Paragraph(String.valueOf(results.getMentalHealth()), resultsFont));
        r8c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r8c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r8c2.setPadding(cellPadding);
        r8c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r8c2);

        PdfPCell r8c3 = new PdfPCell(new Paragraph(
                "Salud mental general, lo que incluye la depresión, la ansiedad, el control de la conducta y el control emocional y el efecto positivo en general.", bodyFont));
        r8c3.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        r8c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r8c3.setPadding(cellPadding);
        r8c3.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r8c3);

        // --- Fila 9: Evolución declarada de la salud ---
        PdfPCell r9c1 = new PdfPCell(new Paragraph("Evolución declarada de la salud", bodyFont));
        r9c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r9c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r9c1.setPadding(cellPadding);
        r9c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r9c1);

        PdfPCell r9c2 = new PdfPCell(new Paragraph(String.valueOf(results.getHealthEvolution()), resultsFont));
        r9c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r9c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r9c2.setPadding(cellPadding);
        r9c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r9c2);

        PdfPCell r9c3 = new PdfPCell(new Paragraph(
                "Valoración de la salud actual comparada con la de un año atrás.", bodyFont));
        r9c3.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        r9c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r9c3.setPadding(cellPadding);
        r9c3.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r9c3);

        // Añadir la tabla al documento
        document.add(table);
    

        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 75, 0);
        ct.setSimpleColumn(30, 30, 930, 800); // coordinates for the right column
        img.setAlignment(Element.ALIGN_RIGHT);
        img.setAlignment(Element.ALIGN_TOP);
        ct.addElement(img);
        ct.go();
        ct.setSimpleColumn(70, 30, 525, 110); // coordinates for the right column
        linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
        ct.addElement(linebreak);
        ct.go();

        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Fecha y hora: " + LocalDateTime.now(ZoneId.of("Europe/Madrid")).format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", a las " + LocalTime.now(ZoneId.of("Europe/Madrid")).format(timeFormatter) , footerFont), 525, 110, 0);
        
        document.close();

        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_SF36_" + sf.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/sf36/", pdfMultipartFile);
            return returned.split(" ")[3];
        } catch (Exception e) {
            throw new IOException("Error uploading file to S3");
        }
    }

    public String generatePedsqlPdf(Pedsql pedsql) throws DocumentException, MalformedURLException, IOException {
        Optional<Patient> optional = patientRepository.findById(pedsql.getPatientId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = optional.get();
        PedsqlResults results = PedsqlService.getResults(pedsql);

        Document document = new Document(PageSize.A4, 70, 70, 60, 60);
        ByteBuffer buffer = new ByteBuffer();
        PdfWriter writer = PdfWriter.getInstance(document, buffer);
        document.open();

        Font titleFont = FontFactory.getFont("Helvetica", 20);
        Paragraph title = new Paragraph("Cuestionario de calidad de vida pediátrica (PedsQL)", titleFont);
        title.setSpacingAfter(-10);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        Chunk linebreak = new Chunk(new LineSeparator());

        document.add(title);
        document.add(linebreak);

        ColumnText ct = new ColumnText(writer.getDirectContent());
        Font leftColumnFont = FontFactory.getFont("Helvetica", 10);
        ct.setSimpleColumn(90, 150, 336, 695); // coordinates for the left column
        Paragraph p1 = new Paragraph("Paciente: " + patient.getSurnames() + ", " + patient.getName(), leftColumnFont);
        Paragraph p2 = new Paragraph("Fecha de nacimiento: " + patient.getBirthdate().format(formatter)  + " ("+ getYearsBetween(patient.getBirthdate(), pedsql.getCompletionDate()) +" años)", leftColumnFont);
        Paragraph p3= new Paragraph("Fecha: " + pedsql.getCompletionDate().format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + " (Sesión " + pedsql.getSession() + ")", leftColumnFont);
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
        p.setSpacingAfter(5);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("El ", bodyFont));
        p.add(new Chunk("PedsQL", boldBodyFont));
        p.add(new Chunk(" (Pediatric Quality of Life Inventory) es un instrumento ampliamente utilizado para evaluar la ", bodyFont));
        p.add(new Chunk("calidad de vida relacionada con la salud", boldBodyFont));
        p.add(new Chunk(" en población pediátrica ", bodyFont));
        p.add(new Chunk("(niños y adolescentes)", boldBodyFont));
        p.add(new Chunk(" Su diseño permite recoger información desde la perspectiva tanto de los propios menores como de sus padres o cuidadores, ofreciendo una visión integral del estado de salud y bienestar en diferentes contextos clínicos y de investigación. Este cuestionario consta de distintas versiones y módulos específicos según la edad y la patología, aunque en su forma genérica abarca áreas fundamentales de la vida del niño:", bodyFont));
        document.add(p);

        String[] boldTexts = {"Función física", "Funcionamiento emocional", "Funcionamiento social", "Funcionamiento escolar"};
        List list = new List(List.ORDERED);
        list.setIndentationLeft(20);
        list.setIndentationRight(20);
        for (int i = 0; i < boldTexts.length; i++) {
            ListItem item = new ListItem();
            item.setFont(boldBodyFont);
            item.setSpacingAfter(i == boldTexts.length - 1 ? 0 : 2);
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            item.add(new Chunk(boldTexts[i], boldBodyFont));
            item.setLeading(0, 1.2f);
            list.add(item);
        }
        document.add(list);

        p = new Paragraph();
        p.setSpacingBefore(5);
        p.setSpacingAfter(10);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("Estas dimensiones permiten obtener un ", bodyFont));
        p.add(new Chunk("perfil amplio", boldBodyFont));
        p.add(new Chunk(" de la ", bodyFont));
        p.add(new Chunk("calidad de vida percibida", boldBodyFont));
        p.add(new Chunk(" y el impacto que pueden tener ", bodyFont));
        p.add(new Chunk("enfermedades crónicas", boldBodyFont));
        p.add(new Chunk(", tratamientos médicos o intervenciones terapéuticas en el desarrollo y la vida cotidiana de los niños.", bodyFont));
        document.add(p);

        subtitle = new Paragraph("Características y utilidad clínica", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        boldTexts = new String[]{"Aplicabilidad: ", "Validez y fiabilidad: ", "Perspectiva múltiple: ", "Comparaciones normativas: "};
        String[] definitions = {"El PedsQL es relativamente breve y está adaptado a diferentes grupos de edad (desde 2 hasta 18 años, con módulos correspondientes a edades preescolares, escolares y adolescentes), lo que facilita su uso en consulta, programas de rehabilitación y estudios de investigación.",
            "Ha sido validado en múltiples países y en un amplio rango de condiciones pediátricas —incluyendo enfermedades crónicas, obesidad infantil o trastornos del desarrollo—, mostrando buena consistencia interna, reproducibilidad y sensibilidad a los cambios en el estado de salud.",
            "Dispone de versiones autoadministradas para los niños (cuando su edad y capacidad de comprensión lo permiten) y versiones paralelas para los padres o cuidadores. Esto permite contrastar la percepción del menor con la valoración de los adultos responsables.", 
            "Existen valores de referencia que posibilitan comparar los resultados de un individuo o grupo con la población general o con grupos específicos de niños con la misma patología."};    
        
        list = new List(List.UNORDERED);
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

        Font footerFont = FontFactory.getFont("Helvetica", 10, Font.ITALIC);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 75, 0);
        ct.setSimpleColumn(70, 30, 525, 110); // coordinates for the right column
        linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
        ct.addElement(linebreak);
        ct.go();

        document.newPage();

        subtitle = new Paragraph("Relevancia en ejercicio terapéutico", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(10);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
        
        p = new Paragraph();
        p.setSpacingAfter(5);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("El ", bodyFont));
        p.add(new Chunk("PedsQL", boldBodyFont));
        p.add(new Chunk(" es especialmente útil en programas de rehabilitación y ejercicio terapéutico dirigidos a población pediátrica, ya que permite capturar cambios tanto en la dimensión física como en la esfera psicosocial de la calidad de vida. En este contexto, el cuestionario resulta de gran utilidad para:", bodyFont));
        document.add(p);

        boldTexts = new String[]{"Monitorizar", "Personalizar", "Evaluar"};
        definitions = new String[]{" la efectividad de las intervenciones de ejercicio en la calidad de vida infantil, ofreciendo indicadores cuantitativos del progreso en el funcionamiento físico y psicosocial.",
            " los programas de actividad física, adaptando la intensidad y la duración de las actividades a las necesidades y evolución de cada niño, considerando también su percepción subjetiva de bienestar.",
            " áreas de mejora específicas, ya que si un niño presenta mayor afectación en el ámbito emocional o social, la intervención se puede orientar de manera más individualizada para abordar estos aspectos."};
        list = new List(List.UNORDERED);
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

        subtitle = new Paragraph("Conclusión y aplicación en la evaluación de la calidad de vida", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(10);
        subtitle.setSpacingBefore(10);
        document.add(subtitle);

        p = new Paragraph();
        p.setSpacingAfter(5);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("Al incorporar el ", bodyFont));
        p.add(new Chunk("PedsQL", boldBodyFont));
        p.add(new Chunk(" en la evaluación de los niños que participan en programas de ejercicio terapéutico, se obtiene una ", bodyFont));
        p.add(new Chunk("medida estandarizada", boldBodyFont));
        p.add(new Chunk(" de cómo perciben su ", bodyFont));
        p.add(new Chunk("salud", boldBodyFont));
        p.add(new Chunk(" y ", bodyFont));
        p.add(new Chunk("bienestar", boldBodyFont));
        p.add(new Chunk(" en diferentes ámbitos de la vida diaria. Este enfoque global resulta clave para:", bodyFont));
        document.add(p);

        definitions = new String[]{"Detectar tempranamente posibles dificultades asociadas a su enfermedad o tratamiento.",
            "Evaluar la evolución del niño a lo largo del programa.",
            "Brindar un soporte integral ajustado a las necesidades físicas, emocionales y sociales de la población pediátrica."};
        list = new List(List.ORDERED);
        list.setIndentationLeft(20);
        list.setIndentationRight(20);
        for (int i = 0; i < boldTexts.length; i++) {
            ListItem item = new ListItem();
            item.setFont(boldBodyFont);
            item.setSpacingAfter(i == boldTexts.length - 1 ? 0 : 2);
            item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
            item.add(new Chunk(definitions[i], bodyFont));
            item.setLeading(0, 1.2f);
            list.add(item);
        }
        document.add(list);

        p = new Paragraph();
        p.setSpacingBefore(5);
        p.setLeading(12);
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        p.add(new Chunk("Con el uso de este cuestionario, la intervención no solo se centra en los componentes puramente físicos de la rehabilitación, sino que abarca la ", bodyFont));
        p.add(new Chunk("perspectiva completa del menor", boldBodyFont));
        p.add(new Chunk(", facilitando la ", bodyFont));
        p.add(new Chunk("toma de decisiones clínicas", boldBodyFont));
        p.add(new Chunk(" y la ", bodyFont));
        p.add(new Chunk("optimización del plan terapéutico", boldBodyFont));
        p.add(new Chunk(".", bodyFont));
        document.add(p);

        subtitle = new Paragraph("Resultados", subtitleFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingBefore(10);
        subtitle.setSpacingAfter(10);
        document.add(subtitle);

        
        // Crear una tabla de 3 columnas
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100); // Ocupa el 100% del ancho disponible
        // Definir anchos relativos de las columnas (por ejemplo, 2:1:5)
        float[] columnWidths = {6f, 2f};
        table.setWidths(columnWidths);

        // Definir un padding (espaciado interno) para las celdas
        int cellPadding = 6;
        float cellPaddingTopRatio = 0.5f;
        
        // Definir el color de la cabecera
        BaseColor headerColor = new BaseColor(98, 151, 208, 100);

        Font resultsFont = FontFactory.getFont("Helvetica", 12, Font.BOLD, new BaseColor(13, 62, 160));

        // --- Fila de cabecera ---
        PdfPCell cell;
        
        cell = new PdfPCell(new Paragraph("Dimensión", boldBodyFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(cellPadding);
        cell.setPaddingTop(cellPadding * cellPaddingTopRatio);
        cell.setBackgroundColor(headerColor);
        table.addCell(cell);

        cell = new PdfPCell(new Paragraph("Puntuación obtenida (0-100)", boldBodyFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(cellPadding);
        cell.setPaddingTop(cellPadding * cellPaddingTopRatio);
        cell.setBackgroundColor(headerColor);
        table.addCell(cell);

        // --- Fila 1: Funcionamiento físico ---
        PdfPCell cell1 = new PdfPCell(new Paragraph("Funcionamiento físico", bodyFont));
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell1.setPadding(cellPadding);
        cell1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Paragraph(String.valueOf(results.getPhysicalFunction()), resultsFont));
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell2.setPadding(cellPadding);
        cell2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(cell2);
        
        // --- Fila 2: Funcionamiento emocional ---
        PdfPCell r2c1 = new PdfPCell(new Paragraph("Funcionamiento emocional", bodyFont));
        r2c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r2c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r2c1.setPadding(cellPadding);
        r2c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r2c1);
        
        PdfPCell r2c2 = new PdfPCell(new Paragraph(String.valueOf(results.getEmotionalFunction()), resultsFont));
        r2c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r2c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r2c2.setPadding(cellPadding);
        r2c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r2c2);
        
        // --- Fila 3: Funcionamiento social ---
        PdfPCell r3c1 = new PdfPCell(new Paragraph("Funcionamiento social", bodyFont));
        r3c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r3c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r3c1.setPadding(cellPadding);
        r3c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r3c1);
        
        PdfPCell r3c2 = new PdfPCell(new Paragraph(String.valueOf(results.getSocialFunction()), resultsFont));
        r3c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r3c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r3c2.setPadding(cellPadding);
        r3c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r3c2);

        // --- Fila 4: Funcionamiento escolar ---
        PdfPCell r4c1 = new PdfPCell(new Paragraph("Funcionamiento escolar", bodyFont));
        r4c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r4c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r4c1.setPadding(cellPadding);
        r4c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r4c1);
        
        PdfPCell r4c2 = new PdfPCell(new Paragraph(String.valueOf(results.getSchoolarFunction()), resultsFont));
        r4c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r4c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r4c2.setPadding(cellPadding);
        r4c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r4c2);
        
        // --- Fila 5: Funcionamiento psicosocial ---
        PdfPCell r5c1 = new PdfPCell(new Paragraph("Funcionamiento psicosocial", bodyFont));
        r5c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r5c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r5c1.setPadding(cellPadding);
        r5c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r5c1);

        PdfPCell r5c2 = new PdfPCell(new Paragraph(String.valueOf(results.getPsychosocialFunction()), resultsFont));
        r5c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r5c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r5c2.setPadding(cellPadding);
        r5c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r5c2);
        
        // --- Fila 6: Función social ---
        PdfPCell r6c1 = new PdfPCell(new Paragraph("Puntuación total", boldBodyFont));
        r6c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        r6c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r6c1.setPadding(cellPadding);
        r6c1.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r6c1);
        
        PdfPCell r6c2 = new PdfPCell(new Paragraph(String.valueOf(results.getTotal()), resultsFont));
        r6c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        r6c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        r6c2.setPadding(cellPadding);
        r6c2.setPaddingTop(cellPadding * cellPaddingTopRatio);
        table.addCell(r6c2);
        
        // Añadir la tabla al documento
        document.add(table);
        
        
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 75, 0);
        ct.setSimpleColumn(30, 30, 930, 800); // coordinates for the right column
        img.setAlignment(Element.ALIGN_RIGHT);
        img.setAlignment(Element.ALIGN_TOP);
        ct.addElement(img);
        ct.go();
        ct.setSimpleColumn(70, 30, 525, 110); // coordinates for the right column
        linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
        ct.addElement(linebreak);
        ct.go();

        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Fecha y hora: " + LocalDateTime.now(ZoneId.of("Europe/Madrid")).format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", a las " + LocalTime.now(ZoneId.of("Europe/Madrid")).format(timeFormatter) , footerFont), 525, 110, 0);
        
        document.close();

        PdfMultipartFile pdfMultipartFile = new PdfMultipartFile(String.valueOf(patient.getId()) + "_PEDSQL_" + pedsql.getCompletionDate() +".pdf", buffer.toByteArray());
        try {
            String returned = s3Service.uploadMultipartFile("pdfs/pedsql/", pdfMultipartFile);
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
        img.scaleToFit(100,100);

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

        Font footerFont = FontFactory.getFont("Helvetica", 10, Font.ITALIC);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 75, 0);
        ct.setSimpleColumn(70, 30, 525, 110); // coordinates for the right column
        linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
        ct.addElement(linebreak);
        ct.go();

        for(Objective objective : objectivesResponse.getObjectives()) {
            document.newPage();

            subtitle = new Paragraph("Objetivo " + (objectivesResponse.getObjectives().indexOf(objective)+1), subtitleFont);
            subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(10);
            subtitle.setSpacingAfter(20);
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

            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 75, 0);
            ct.setSimpleColumn(30, 30, 930, 800); // coordinates for the right column
            img.setAlignment(Element.ALIGN_RIGHT);
            img.setAlignment(Element.ALIGN_TOP);
            ct.addElement(img);
            ct.go();
            ct.setSimpleColumn(70, 30, 525, 110); // coordinates for the right column
            linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
            ct.addElement(linebreak);
            ct.go();
        }

        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Fecha y hora: " + LocalDateTime.now(ZoneId.of("Europe/Madrid")).format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", a las " + LocalTime.now(ZoneId.of("Europe/Madrid")).format(timeFormatter) , footerFont), 525, 110, 0);
        
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
        img.scaleToFit(100,100);

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

        Font footerFont = FontFactory.getFont("Helvetica", 10, Font.ITALIC);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 75, 0);
        ct.setSimpleColumn(70, 30, 525, 110); // coordinates for the right column
        linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
        ct.addElement(linebreak);
        ct.go();


        for(Prescription prescription : prescriptionsResponse.getPrescriptions()) {
            document.newPage();
            
            writer.setMargins(70, 70, 70, 60);

            subtitle = new Paragraph("Ejercicio " + (prescriptionsResponse.getPrescriptions().indexOf(prescription)+1), subtitleFont);
            subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(10);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            addPrescriptionElement(document, "Ejercicio", prescription.getExercise());
            addPrescriptionElement(document, "Modalidad", prescription.getModality());
            addPrescriptionElement(document, "Frecuencia", prescription.getFrequency());
            addPrescriptionElement(document, "Intensidad", prescription.getIntensity());
            addPrescriptionElement(document, "Tiempo", prescription.getTime());
            addPrescriptionElement(document, "Tipo", prescription.getType());
            addPrescriptionElement(document, "Volumen", prescription.getVolume());
            addPrescriptionElement(document, "Progresión", prescription.getProgression());

            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 75, 0);
            ct.setSimpleColumn(30, 30, 930, 800); // coordinates for the right column
            img.setAlignment(Element.ALIGN_RIGHT);
            img.setAlignment(Element.ALIGN_TOP);
            ct.addElement(img);
            ct.go();
            ct.setSimpleColumn(70, 30, 525, 110); // coordinates for the right column
            linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
            ct.addElement(linebreak);
            ct.go();
        }

        if(prescriptionsResponse.getPrescriptions().get(0).getSpecialConsiderations() != null && !prescriptionsResponse.getPrescriptions().get(0).getSpecialConsiderations().isEmpty()) {
            document.newPage();

            subtitle = new Paragraph("Consideraciones especiales", subtitleFont);
            subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            subtitle.setSpacingBefore(10);
            subtitle.setSpacingAfter(15);
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
                item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
                item.add(new Chunk(line, bodyFont));
                list.add(item);
            }
            document.add(list); 

            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Pág. " + writer.getPageNumber(), bodyFont), 500, 75, 0);
            ct.setSimpleColumn(30, 30, 930, 800); // coordinates for the right column
            img.setAlignment(Element.ALIGN_RIGHT);
            img.setAlignment(Element.ALIGN_TOP);
            ct.addElement(img);
            ct.go();
            ct.setSimpleColumn(70, 30, 525, 110); // coordinates for the right column
            linebreak = new Chunk(new LineSeparator(3.0F, 100.0F, new BaseColor(98, 151, 208), Element.ALIGN_BOTTOM, 0.0F));
            ct.addElement(linebreak);
            ct.go();
        }

        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, new Phrase("Fecha y hora: " + LocalDateTime.now(ZoneId.of("Europe/Madrid")).format(formatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)) + ", a las " + LocalTime.now(ZoneId.of("Europe/Madrid")).format(timeFormatter) , footerFont), 525, 110, 0);

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
            p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
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
        item.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
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
        p.setAlignment(com.itextpdf.text.Element.ALIGN_JUSTIFIED);
        document.add(p);
    }

    private static int getYearsBetween(LocalDate date1, LocalDate date2) {
        return Period.between(date1, date2).getYears();
    }

}
