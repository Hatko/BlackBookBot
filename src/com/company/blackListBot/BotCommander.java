package com.company.blackListBot;

import com.company.data.Complaint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vlad on 11/13/16.
 */
interface IBotCommanderDelegate {
    void changeCommandState(BotCommander commander);

    void printMessage(String msg, BotCommander commander);

    void addNumberToBlackList(Complaint complaint);

    void canBeRemoved(BotCommander commander);

    ArrayList<Complaint> fetchAllComplaints(String number);

    ArrayList<Complaint> fetchAllComplaintsForUser(String userId);

    Complaint existedComplaint(String userId, String number);
}

class BotCommander {
    String userId;
    private IBotCommanderDelegate delegate;

    BotCommander(String userId, IBotCommanderDelegate delegate) {
        this.userId = userId;
        this.delegate = delegate;
    }

    BotCommanderState getState() {
        return _state;
    }

    private void setState(BotCommanderState state) {
        if (_state != state) {
            _state = state;

            if (_state == BotCommanderState.Root) {
                delegate.canBeRemoved(this);
            }
//            new Thread(() -> delegate.changeCommandState(this)).start();
        }
    }

    void processInput(String input) {
        if (input == null) {
            return;
        }

        switch (getState()) {
            case Root:
                break;
            case AddNewNumber:
                if (ComplaintBuilder.validatePhoneNumber(input)) {
                    Complaint existedComplaint = delegate.existedComplaint(userId, input);

                    if (existedComplaint != null) {
                        delegate.printMessage("You've already added your complaint for this number:", this);
                        delegate.printMessage(existedComplaint.description, this);
                    } else {
                        complaintBuilder = new ComplaintBuilder(input);
                        setState(BotCommanderState.AddComplaint);
                        delegate.printMessage("Add description to your complaint, if you want to; you can also add tags with ~yourFirstTag ~yourSecondTag, separated by whitespaces", this);
                    }
                } else {
                    numberHasIncorrectFormatError();
                }
                break;
            case AddComplaint: {
                Complaint complaint = complaintBuilder.complaintFromInput(input);
                complaint.userId = userId;
                complaint.date = Instant.now().getEpochSecond();
                delegate.addNumberToBlackList(complaint);
            }
            break;
            case CheckNumber: {
                if (ComplaintBuilder.validatePhoneNumber(input)) {
                    ArrayList<Complaint> complaints = delegate.fetchAllComplaints(input);

                    if (complaints.size() > 0) {
                        for (Complaint complaint : complaints) {
                            delegate.printMessage(complaint.description, this);
                        }
                    } else {
                        delegate.printMessage("This number is not found", this);
                    }
                } else {
                    numberHasIncorrectFormatError();
                }
            }
            break;
        }
    }

    void processCommand(String command) {
        switch (command) {
            case "/add":
                setState(BotCommanderState.AddNewNumber);
                delegate.printMessage("Enter new number to be added to black list:", this);
                break;
            case "/check":
                setState(BotCommanderState.CheckNumber);
                delegate.printMessage("Enter number you want to check:", this);
                break;
            case "/view":
                ArrayList<Complaint> complaints = delegate.fetchAllComplaintsForUser(userId);

                for (Complaint complaint : complaints) {
                    delegate.printMessage(complaint.description, this);
                }
            default:
        }
    }

    enum BotCommanderState {
        Root(0), AddNewNumber(1), AddComplaint(2), CheckNumber(3);

        int value;

        BotCommanderState(int value) {
            this.value = value;
        }

        private static Map<Integer, BotCommanderState> map = new HashMap<>();

        static {
            for (BotCommanderState legEnum : BotCommanderState.values()) {
                map.put(legEnum.value, legEnum);
            }
        }

        public static BotCommanderState valueOf(int stateIdx) {
            return map.get(stateIdx);
        }

        @Override
        public java.lang.String toString() {
            switch (this) {
                case Root:
                    return "root";
                case AddNewNumber:
                    return "addNewNumber";
            }

            return super.toString();
        }
    }

    private BotCommanderState _state = BotCommanderState.Root;
    private ComplaintBuilder complaintBuilder;

    private void numberHasIncorrectFormatError() {
        delegate.printMessage("Number is in incorrect format; try something like +380965555555", this);
    }
}
