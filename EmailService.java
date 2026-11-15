package com.mindsmart.service;

import com.mindsmart.entity.ApplicationStatus;
import com.mindsmart.entity.EmailLog;
import com.mindsmart.repository.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private final EmailLogRepository emailLogRepository;

    @Value("${mindsmart.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${mindsmart.mail.from:noreply@mindsmart.com}")
    private String fromEmail;

    public EmailService(EmailLogRepository emailLogRepository) {
        this.emailLogRepository = emailLogRepository;
    }

    public void sendEmail(String toEmail, String subject, String htmlContent) {
        String status = "SIMULATED_DEMO";

        if (mailEnabled && mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromEmail, "MindSmart Platform");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                status = "SENT";
                logger.info("[MindSmart Email] Successfully dispatched real SMTP email to: {}", toEmail);
            } catch (Exception e) {
                logger.warn("[MindSmart Email] Real SMTP dispatch failed ({}), recording to Demo Email Simulator: {}", e.getMessage(), toEmail);
                status = "SIMULATED_DEMO";
            }
        } else {
            logger.info("[MindSmart Demo Mode] Mail server not enabled or unconfigured. Simulated email to: [{}] Subject: [{}]", toEmail, subject);
        }

        try {
            EmailLog log = new EmailLog(toEmail, subject, htmlContent, status);
            emailLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to save email log: {}", e.getMessage());
        }
    }

    public void sendRegistrationConfirmation(String email, String name, String role) {
        String subject = "Welcome to MindSmart – Smart Internship Opportunities Platform!";
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;'>"
                + "<div style='background: linear-gradient(135deg, #6366f1, #a855f7, #ec4899); padding: 25px; border-radius: 8px; text-align: center; color: white;'>"
                + "<h1 style='margin: 0; font-size: 24px;'>Welcome to MindSmart!</h1>"
                + "<p style='margin: 5px 0 0 0; opacity: 0.9;'>Smart Internship Opportunities Platform</p>"
                + "</div>"
                + "<div style='padding: 20px 10px; color: #334155; line-height: 1.6;'>"
                + "<p>Hello <strong>" + name + "</strong>,</p>"
                + "<p>Thank you for registering on <strong>MindSmart</strong> as a <strong>" + role.replace("ROLE_", "") + "</strong>.</p>"
                + "<p>You can now log in to explore personalized internships, leverage our AI-driven resume skill matcher, and accelerate your career journey.</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='http://localhost:8080' style='background: #6366f1; color: white; padding: 12px 25px; border-radius: 8px; text-decoration: none; font-weight: bold;'>Go to MindSmart Dashboard</a>"
                + "</div>"
                + "<p style='font-size: 13px; color: #64748b;'>If you did not create this account, please ignore this email.</p>"
                + "</div>"
                + "</div>";

        sendEmail(email, subject, content);
    }

    public void sendApplicationConfirmation(String email, String studentName, String internshipTitle, String companyName) {
        String subject = "Application Submitted: " + internshipTitle + " at " + companyName;
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;'>"
                + "<div style='background: linear-gradient(135deg, #6366f1, #06b6d4); padding: 25px; border-radius: 8px; text-align: center; color: white;'>"
                + "<h2 style='margin: 0;'>Application Received!</h2>"
                + "<p style='margin: 5px 0 0 0; opacity: 0.9;'>" + companyName + "</p>"
                + "</div>"
                + "<div style='padding: 20px 10px; color: #334155; line-height: 1.6;'>"
                + "<p>Dear <strong>" + studentName + "</strong>,</p>"
                + "<p>Your application for the position of <strong>" + internshipTitle + "</strong> at <strong>" + companyName + "</strong> has been successfully submitted!</p>"
                + "<p>The recruiting team has received your profile and resume. You can track the real-time status of your application from your MindSmart student dashboard.</p>"
                + "<div style='background: #f8fafc; border-left: 4px solid #6366f1; padding: 12px 15px; margin: 20px 0;'>"
                + "<p style='margin: 0; font-size: 14px;'><strong>Status:</strong> PENDING REVIEW</p>"
                + "</div>"
                + "<p>Best of luck with your application!</p>"
                + "</div>"
                + "</div>";

        sendEmail(email, subject, content);
    }

    public void sendStatusUpdateEmail(String email, String studentName, String internshipTitle, String companyName,
                                      ApplicationStatus status, String interviewDate, String interviewLink, String notes) {
        String subject = "Application Status Update: " + internshipTitle + " - " + status;
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;'>")
          .append("<div style='background: linear-gradient(135deg, #4f46e5, #06b6d4); padding: 25px; border-radius: 8px; text-align: center; color: white;'>")
          .append("<h2 style='margin: 0;'>Application Status Update</h2>")
          .append("<p style='margin: 5px 0 0 0; opacity: 0.9;'>").append(internshipTitle).append(" at ").append(companyName).append("</p>")
          .append("</div>")
          .append("<div style='padding: 20px 10px; color: #334155; line-height: 1.6;'>")
          .append("<p>Dear <strong>").append(studentName).append("</strong>,</p>")
          .append("<p>Your application status for <strong>").append(internshipTitle).append("</strong> has been updated to: </p>")
          .append("<div style='text-align: center; margin: 20px 0;'>")
          .append("<span style='background: #6366f1; color: white; padding: 10px 20px; border-radius: 20px; font-weight: bold; font-size: 16px;'>")
          .append(status.name().replace("_", " "))
          .append("</span></div>");

        if (status == ApplicationStatus.INTERVIEW_SCHEDULED) {
            sb.append("<div style='background: #ecfdf5; border: 1px solid #a7f3d0; padding: 15px; border-radius: 8px; margin: 20px 0;'>")
              .append("<h3 style='margin: 0 0 10px 0; color: #065f46;'>Interview Details</h3>")
              .append("<p style='margin: 4px 0;'><strong>Date & Time:</strong> ").append(interviewDate != null ? interviewDate : "To be confirmed").append("</p>");
            if (interviewLink != null && !interviewLink.isBlank()) {
                sb.append("<p style='margin: 4px 0;'><strong>Meeting Link / Location:</strong> <a href='").append(interviewLink).append("'>").append(interviewLink).append("</a></p>");
            }
            if (notes != null && !notes.isBlank()) {
                sb.append("<p style='margin: 4px 0;'><strong>Instructions:</strong> ").append(notes).append("</p>");
            }
            sb.append("</div>");
        }

        sb.append("<p>Log in to your MindSmart dashboard for further details and messaging.</p>")
          .append("</div></div>");

        sendEmail(email, subject, sb.toString());
    }

    public void sendDeadlineReminder(String email, String studentName, String internshipTitle, String deadline) {
        String subject = "Reminder: Application Deadline Closing Soon for " + internshipTitle;
        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px; background: #ffffff;'>"
                + "<div style='background: linear-gradient(135deg, #f59e0b, #ef4444); padding: 25px; border-radius: 8px; text-align: center; color: white;'>"
                + "<h2 style='margin: 0;'>Application Deadline Approaching!</h2>"
                + "</div>"
                + "<div style='padding: 20px 10px; color: #334155; line-height: 1.6;'>"
                + "<p>Hi <strong>" + studentName + "</strong>,</p>"
                + "<p>The internship <strong>" + internshipTitle + "</strong> that you saved is closing on <strong>" + deadline + "</strong>.</p>"
                + "<p>Don't miss the chance to submit your application before submissions close!</p>"
                + "</div>"
                + "</div>";

        sendEmail(email, subject, content);
    }

    public List<EmailLog> getRecentEmailLogs() {
        return emailLogRepository.findTop20ByOrderBySentAtDesc();
    }
}
