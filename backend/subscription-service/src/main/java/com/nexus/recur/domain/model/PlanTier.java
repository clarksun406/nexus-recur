package com.nexus.recur.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "plan_tiers", indexes = {
        @Index(name = "idx_tier_plan", columnList = "planId")
})
public class PlanTier {
    @Id
    @Column(length = 32)
    private String id;
    @Column(nullable = false, length = 32)
    private String planId;
    @Column(nullable = false)
    private int tierStart;
    private Integer tierEnd;
    @Column(nullable = false)
    private int unitAmountCents;
    private int flatAmountCents;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public int getTierStart() { return tierStart; }
    public void setTierStart(int tierStart) { this.tierStart = tierStart; }
    public Integer getTierEnd() { return tierEnd; }
    public void setTierEnd(Integer tierEnd) { this.tierEnd = tierEnd; }
    public int getUnitAmountCents() { return unitAmountCents; }
    public void setUnitAmountCents(int unitAmountCents) { this.unitAmountCents = unitAmountCents; }
    public int getFlatAmountCents() { return flatAmountCents; }
    public void setFlatAmountCents(int flatAmountCents) { this.flatAmountCents = flatAmountCents; }
}
