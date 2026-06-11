package com.qbank.dto;

public class OptionDTO {
    private String optionText;
    private boolean isCorrect;

    public OptionDTO() {}

    public OptionDTO(String optionText, boolean isCorrect) {
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }

    // Explicit Getters and Setters
    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        this.isCorrect = correct;
    }
}