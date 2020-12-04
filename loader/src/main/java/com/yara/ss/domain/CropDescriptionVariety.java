package com.yara.ss.domain;

import org.eclipse.rdf4j.query.algebra.Str;

import java.util.Objects;

public class CropDescriptionVariety {

    private String id;
    private String varId;
    private String descId;

    public CropDescriptionVariety(String id, String varId, String descId) {
        this.id = id;
        this.varId = varId;
        this.descId = descId;
    }

    public String getId() {
        return id;
    }

    public String getVarId() {
        return varId;
    }

    public String getDescId() {
        return descId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropDescriptionVariety that = (CropDescriptionVariety) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(varId, that.varId) &&
                Objects.equals(descId, that.descId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, varId, descId);
    }
}
