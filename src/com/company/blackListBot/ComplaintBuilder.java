package com.company.blackListBot;

import com.company.data.Complaint;
import com.company.data.Tag;

import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vlad on 11/20/16.
 */
class ComplaintBuilder {
    ComplaintBuilder(String number) {
        this.number = number;
    }

    static boolean validatePhoneNumber(String number) {
        String phoneRegEx = "\\d{10}|\\+?\\d{12}";

        return number.matches(phoneRegEx);
    }

    Complaint complaintFromInput(String input) {
        Complaint complaint = new Complaint();

        LinkedHashSet<Tag> tags = new LinkedHashSet<>();

        Matcher matcher = Pattern.compile("(~\\w*)").matcher(input);
        while (matcher.find()) {
            Tag tag = new Tag(matcher.group(1));

            tags.add(tag);
        }

        complaint.description = input;
        complaint.number = number;

        complaint.tags = new Tag[tags.size()];
        tags.toArray(complaint.tags);

        return complaint;
    }

    private String number;
}
