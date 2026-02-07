package com.rentease.service;

import com.rentease.entity.Booking;
import com.rentease.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@rentease.com}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @Async
    public void sendBookingRequestEmail(Booking booking) {
        User owner = booking.getListing().getOwner();
        User renter = booking.getRenter();

        String subject = "New Booking Request for " + booking.getListing().getTitle();
        String body = buildEmailTemplate(
            owner.getFirstName(),
            "New Booking Request",
            String.format(
                "<p><strong>%s %s</strong> wants to rent your <strong>%s</strong>.</p>" +
                "<p><strong>Dates:</strong> %s - %s</p>" +
                "<p><strong>Total:</strong> $%.2f</p>" +
                "%s" +
                "<p>Please respond to this request within 24 hours.</p>",
                renter.getFirstName(),
                renter.getLastName(),
                booking.getListing().getTitle(),
                booking.getStartDate().format(DATE_FORMAT),
                booking.getEndDate().format(DATE_FORMAT),
                booking.getTotalPrice() / 100.0,
                booking.getRenterMessage() != null ?
                    "<p><strong>Message:</strong> " + booking.getRenterMessage() + "</p>" : ""
            ),
            frontendUrl + "/booking-requests",
            "View Request"
        );

        sendEmail(owner.getEmail(), subject, body);
    }

    @Async
    public void sendBookingApprovedEmail(Booking booking) {
        User renter = booking.getRenter();
        User owner = booking.getListing().getOwner();

        String subject = "Your Booking Request was Approved!";
        String body = buildEmailTemplate(
            renter.getFirstName(),
            "Booking Approved",
            String.format(
                "<p>Great news! <strong>%s</strong> has approved your booking for <strong>%s</strong>.</p>" +
                "<p><strong>Dates:</strong> %s - %s</p>" +
                "<p><strong>Pickup Location:</strong> %s</p>" +
                "%s" +
                "<p>Contact the owner to arrange pickup details.</p>",
                owner.getFirstName(),
                booking.getListing().getTitle(),
                booking.getStartDate().format(DATE_FORMAT),
                booking.getEndDate().format(DATE_FORMAT),
                booking.getListing().getPickupLocation(),
                booking.getOwnerResponse() != null ?
                    "<p><strong>Owner's Message:</strong> " + booking.getOwnerResponse() + "</p>" : ""
            ),
            frontendUrl + "/bookings/" + booking.getId(),
            "View Booking"
        );

        sendEmail(renter.getEmail(), subject, body);
    }

    @Async
    public void sendBookingDeclinedEmail(Booking booking) {
        User renter = booking.getRenter();
        User owner = booking.getListing().getOwner();

        String subject = "Your Booking Request was Declined";
        String body = buildEmailTemplate(
            renter.getFirstName(),
            "Booking Declined",
            String.format(
                "<p>Unfortunately, <strong>%s</strong> was unable to accept your booking for <strong>%s</strong>.</p>" +
                "<p><strong>Requested Dates:</strong> %s - %s</p>" +
                "%s" +
                "<p>Don't worry! There are plenty of other items available. Browse our listings to find something else.</p>",
                owner.getFirstName(),
                booking.getListing().getTitle(),
                booking.getStartDate().format(DATE_FORMAT),
                booking.getEndDate().format(DATE_FORMAT),
                booking.getOwnerResponse() != null ?
                    "<p><strong>Reason:</strong> " + booking.getOwnerResponse() + "</p>" : ""
            ),
            frontendUrl + "/listings",
            "Browse Listings"
        );

        sendEmail(renter.getEmail(), subject, body);
    }

    @Async
    public void sendBookingCancelledEmail(Booking booking, User cancelledBy) {
        User recipient;
        String canceller;

        if (cancelledBy.getId().equals(booking.getRenter().getId())) {
            recipient = booking.getListing().getOwner();
            canceller = "The renter";
        } else {
            recipient = booking.getRenter();
            canceller = "The owner";
        }

        String subject = "Booking Cancelled - " + booking.getListing().getTitle();
        String body = buildEmailTemplate(
            recipient.getFirstName(),
            "Booking Cancelled",
            String.format(
                "<p>%s has cancelled the booking for <strong>%s</strong>.</p>" +
                "<p><strong>Dates:</strong> %s - %s</p>" +
                "%s",
                canceller,
                booking.getListing().getTitle(),
                booking.getStartDate().format(DATE_FORMAT),
                booking.getEndDate().format(DATE_FORMAT),
                booking.getCancellationReason() != null ?
                    "<p><strong>Reason:</strong> " + booking.getCancellationReason() + "</p>" : ""
            ),
            frontendUrl + "/my-bookings",
            "View Bookings"
        );

        sendEmail(recipient.getEmail(), subject, body);
    }

    @Async
    public void sendBookingCompletedEmail(Booking booking) {
        User renter = booking.getRenter();

        String subject = "How was your rental? Leave a review!";
        String body = buildEmailTemplate(
            renter.getFirstName(),
            "Rental Complete",
            String.format(
                "<p>Your rental of <strong>%s</strong> has been marked as complete.</p>" +
                "<p>We hope you had a great experience! Please take a moment to leave a review and help other renters.</p>",
                booking.getListing().getTitle()
            ),
            frontendUrl + "/bookings/" + booking.getId(),
            "Leave a Review"
        );

        sendEmail(renter.getEmail(), subject, body);
    }

    @Async
    public void sendNewMessageEmail(User recipient, User sender, String listingTitle) {
        String subject = "New message from " + sender.getFirstName();
        String body = buildEmailTemplate(
            recipient.getFirstName(),
            "New Message",
            String.format(
                "<p><strong>%s %s</strong> sent you a message about <strong>%s</strong>.</p>" +
                "<p>Log in to RentEase to view and reply to the message.</p>",
                sender.getFirstName(),
                sender.getLastName(),
                listingTitle
            ),
            frontendUrl + "/messages",
            "View Messages"
        );

        sendEmail(recipient.getEmail(), subject, body);
    }

    private String buildEmailTemplate(String recipientName, String heading, String content, String buttonUrl, String buttonText) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f5f5;">
                <div style="max-width: 600px; margin: 0 auto; padding: 40px 20px;">
                    <div style="background-color: white; border-radius: 16px; padding: 40px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
                        <!-- Logo -->
                        <div style="text-align: center; margin-bottom: 32px;">
                            <h1 style="color: #7c3aed; font-size: 28px; margin: 0;">RentEase</h1>
                        </div>

                        <!-- Heading -->
                        <h2 style="color: #1f2937; font-size: 24px; margin-bottom: 24px; text-align: center;">%s</h2>

                        <!-- Greeting -->
                        <p style="color: #4b5563; font-size: 16px; line-height: 1.6;">Hi %s,</p>

                        <!-- Content -->
                        <div style="color: #4b5563; font-size: 16px; line-height: 1.6; margin-bottom: 32px;">
                            %s
                        </div>

                        <!-- CTA Button -->
                        <div style="text-align: center; margin-bottom: 32px;">
                            <a href="%s" style="display: inline-block; background-color: #7c3aed; color: white; text-decoration: none; padding: 14px 32px; border-radius: 50px; font-weight: 600; font-size: 16px;">%s</a>
                        </div>

                        <!-- Footer -->
                        <div style="border-top: 1px solid #e5e7eb; padding-top: 24px; text-align: center;">
                            <p style="color: #9ca3af; font-size: 14px; margin: 0;">
                                This email was sent by RentEase. If you didn't expect this email, you can safely ignore it.
                            </p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, heading, recipientName, content, buttonUrl, buttonText);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}
