package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payroll_column_preference")
public class PayrollColumnPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "column_key", nullable = false, unique = true)
    private String columnKey;

    @Column(name = "visible", nullable = false)
    private boolean visible = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getColumnKey() { return columnKey; }
    public void setColumnKey(String columnKey) { this.columnKey = columnKey; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}