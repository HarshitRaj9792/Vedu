package org.example.videocall.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.example.videocall.model.Schedules;
import org.example.videocall.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmailService {

    // Optional — null when SMTP is disabled (spring.mail.host not configured with real server)
    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:no-reply@vedu.com}")
    private String senderEmail;

    @Async
    public void sendClassNotification(Schedules schedule, List<Student> students) {
        if (javaMailSender == null) {
            log.warn("SMTP not configured — skipping email notification for '{}'", schedule.getTopicName());
            return;
        }
        if (students == null || students.isEmpty()) {
            log.info("No students found for course '{}' and class '{}'. Skipping email notification.", schedule.getTopicCourse(), schedule.getTopicClass());
            return;
        }

        log.info("Preparing to send scheduling email to {} students for topic '{}'", students.size(), schedule.getTopicName());

        for (Student student : students) {
            if (student.getEmail() != null && !student.getEmail().isEmpty()) {
                sendHtmlEmail(student.getEmail(), student.getFullName(), schedule);
            }
        }
        
        log.info("Finished dispatching class notifications.");
    }

    private void sendHtmlEmail(String toAddress, String studentName, Schedules schedule) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toAddress);
            helper.setSubject("New Class Scheduled: " + schedule.getTopicName());

            String htmlContent = "<html><body style='font-family: Arial, sans-serif; color: #333; line-height: 1.6;'>"
                    + "<div style='background-color: #f4f4f4; padding: 20px;'>"
                    + "<div style='background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);'>"
                    + "<h2 style='color: #3c52ff;'>Virtual Classroom Update 💠</h2>"
                    + "<p>Hi <b>" + studentName + "</b>,</p>"
                    + "<p>A new class has been scheduled for your batch. Please find the details below:</p>"
                    + "<table style='width: 100%; border-collapse: collapse; margin-top: 20px; margin-bottom: 20px;'>"
                    + "<tr><td style='padding: 10px; border: 1px solid #ddd;'><b>Topic</b></td><td style='padding: 10px; border: 1px solid #ddd;'>" + schedule.getTopicName() + "</td></tr>"
                    + "<tr><td style='padding: 10px; border: 1px solid #ddd;'><b>Teacher</b></td><td style='padding: 10px; border: 1px solid #ddd;'>" + schedule.getTeacherName() + "</td></tr>"
                    + "<tr><td style='padding: 10px; border: 1px solid #ddd;'><b>Scheduled Time</b></td><td style='padding: 10px; border: 1px solid #ddd;'>" + schedule.getTopicTime() + "</td></tr>"
                    + "</table>"
                    + "<p>Make sure to join on time via your Student Dashboard!</p>"
                    + "<br>"
                    + "<p style='font-size: 12px; color: #888;'>Powered by Vedu.</p>"
                    + "</div></div></body></html>";

            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.debug("Sent class notification to {}", toAddress);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", toAddress, e);
        }
    }
}
