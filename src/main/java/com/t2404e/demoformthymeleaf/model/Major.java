package com.t2404e.demoformthymeleaf.model;

// Enum này sẽ là nguồn dữ liệu cho thẻ <select> trong form
public enum Major {
    INFORMATION_TECHNOLOGY("Công nghệ thông tin"),
    BUSINESS_ADMINISTRATION("Quản trị kinh doanh"),
    GRAPHIC_DESIGN("Thiết kế đồ họa"),
    MARKETING("Marketing");

    private final String displayName;

    Major(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}