package com.yara.ss.domain;

import java.util.*;

public class Fertilizers extends Thing {

    private final Map<String, String> nutrientUnitsContent;

    private String family;
    private String type;
    private String name;
    private String lowChloride;
    private String dryMatter;
    private String spreaderLoss;
    private String density;
    private String utilizationN;
    private String utilizationNh4;
    private String tank;
    private String electricalConductivity;
    private String pH;
    private String solubility5C;
    private String solubility20C;
    private String dhCode;
    private String syncId;
    private String syncSource;
    private String lastSync;
//    private String applicationTags;
//    private String isAvailable;
//    private String localizedName;
//    private String fertilizerSourceSystem;
//    private String prodCountryIdRef;
//    private String prodRegionIdRef;
    private String n;
    private String nUnitId;
    private String p;
    private String pUnitId;
    private String k;
    private String kUnitId;
    private String mg;
    private String mgUnitId;
    private String s;
    private String sUnitId;
    private String ca;
    private String caUnitId;
    private String b;
    private String bUnitId;
    private String zn;
    private String znUnitId;
    private String mn;
    private String mnUnitId;
    private String cu;
    private String cuUnitId;
    private String fe;
    private String feUnitId;
    private String mo;
    private String moUnitId;
    private String na;
    private String naUnitId;
    private String se;
    private String seUnitId;
    private String co;
    private String coUnitId;
    private String no3;
    private String nh4;
    private String urea;

    private Fertilizers(Builder builder) {
        super(builder.source, builder.className, builder.id);
        this.name = builder.name;
        this.family = builder.family;
        this.type = builder.type;
        this.lowChloride = builder.lowChloride;
        this.dryMatter = builder.dryMatter;
        this.spreaderLoss = builder.spreaderLoss;
        this.density = builder.density;
        this.nutrientUnitsContent = builder.nutrientUnitsContent;
        this.n = builder.n;
        this.nUnitId = builder.nUnitId;
        this.p = builder.p;
        this.pUnitId = builder.pUnitId;
        this.k = builder.k;
        this.kUnitId = builder.kUnitId;
        this.mg = builder.mg;
        this.mgUnitId = builder.mgUnitId;
        this.s = builder.s;
        this.sUnitId = builder.sUnitId;
        this.ca = builder.ca;
        this.caUnitId = builder.caUnitId;
        this.b = builder.b;
        this.bUnitId = builder.bUnitId;
        this.zn = builder.zn;
        this.znUnitId = builder.znUnitId;
        this.mn = builder.mn;
        this.mnUnitId = builder.mnUnitId;
        this.cu = builder.cu;
        this.cuUnitId = builder.cuUnitId;
        this.fe = builder.fe;
        this.feUnitId = builder.feUnitId;
        this.mo = builder.mo;
        this.moUnitId = builder.moUnitId;
        this.na = builder.na;
        this.naUnitId = builder.naUnitId;
        this.se = builder.se;
        this.seUnitId = builder.seUnitId;
        this.co = builder.co;
        this.coUnitId = builder.coUnitId;
        this.no3 = builder.no3;
        this.nh4 = builder.nh4;
        this.urea = builder.urea;
        this.utilizationN = builder.utilizationN;
        this.utilizationNh4 = builder.utilizationNh4;
        this.tank = builder.tank;
        this.electricalConductivity = builder.electricalConductivity;
        this.pH = builder.pH;
        this.solubility5C = builder.solubility5C;
        this.solubility20C = builder.solubility20C;
        this.dhCode = builder.dhCode;
        this.syncId = builder.syncId;
        this.syncSource = builder.syncSource;
        this.lastSync = builder.lastSync;
//        this.applicationTags = builder.applicationTags;
//        this.isAvailable = builder.isAvailable;
//        this.localizedName = builder.localizedName;
//        this.fertilizerSourceSystem = builder.fertilizerSourceSystem;
//        this.prodCountryIdRef = builder.prodCountryIdRef;
//        this.prodRegionIdRef = builder.prodRegionIdRef;
    }

    public Map<String, String> getNutrientUnitsContent() {
        return nutrientUnitsContent;
    }

    public String getFamily() {
        return family;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getLowChloride() {
        return lowChloride;
    }

    public String getDryMatter() {
        return dryMatter;
    }

    public String getSpreaderLoss() {
        return spreaderLoss;
    }

    public String getDensity() {
        return density;
    }

    public String getUtilizationN() {
        return utilizationN;
    }

    public String getUtilizationNh4() {
        return utilizationNh4;
    }

    public String getTank() {
        return tank;
    }

    public String getElectricalConductivity() {
        return electricalConductivity;
    }

    public String getPh() {
        return pH;
    }

    public String getSolubility5C() {
        return solubility5C;
    }

    public String getSolubility20C() {
        return solubility20C;
    }

    public String getDhCode() {
        return dhCode;
    }

    public String getSyncId() {
        return syncId;
    }

    public String getSyncSource() {
        return syncSource;
    }

    public String getLastSync() {
        return lastSync;
    }

    public String getN() {
        return n;
    }

    public String getNUnitId() {
        return nUnitId;
    }

    public String getP() {
        return p;
    }

    public String getPUnitId() {
        return pUnitId;
    }

    public String getK() {
        return k;
    }

    public String getKUnitId() {
        return kUnitId;
    }

    public String getMg() {
        return mg;
    }

    public String getMgUnitId() {
        return mgUnitId;
    }

    public String getS() {
        return s;
    }

    public String getSUnitId() {
        return sUnitId;
    }

    public String getCa() {
        return ca;
    }

    public String getCaUnitId() {
        return caUnitId;
    }

    public String getB() {
        return b;
    }

    public String getBUnitId() {
        return bUnitId;
    }

    public String getZn() {
        return zn;
    }

    public String getZnUnitId() {
        return znUnitId;
    }

    public String getMn() {
        return mn;
    }

    public String getMnUnitId() {
        return mnUnitId;
    }

    public String getCu() {
        return cu;
    }

    public String getCuUnitId() {
        return cuUnitId;
    }

    public String getFe() {
        return fe;
    }

    public String getFeUnitId() {
        return feUnitId;
    }

    public String getMo() {
        return mo;
    }

    public String getMoUnitId() {
        return moUnitId;
    }

    public String getNa() {
        return na;
    }

    public String getNaUnitId() {
        return naUnitId;
    }

    public String getSe() {
        return se;
    }

    public String getSeUnitId() {
        return seUnitId;
    }

    public String getCo() {
        return co;
    }

    public String getCoUnitId() {
        return coUnitId;
    }

    public String getNo3() {
        return no3;
    }

    public String getNh4() {
        return nh4;
    }

    public String getUrea() {
        return urea;
    }

//    public String getApplicationTags() {
//        return applicationTags;
//    }
//
//    public String getIsAvailable() {
//        return isAvailable;
//    }
//
//    public String getLocalizedName() {
//        return localizedName;
//    }
//
//    public String getFertilizerSourceSystem() {
//        return fertilizerSourceSystem;
//    }
//
//    public String getProdCountryIdRef() {
//        return prodCountryIdRef;
//    }
//
//    public String getProdRegionIdRef() {
//        return prodRegionIdRef;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fertilizers that = (Fertilizers) o;
        return Objects.equals(nutrientUnitsContent, that.nutrientUnitsContent) &&
                Objects.equals(family, that.family) &&
                Objects.equals(type, that.type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(lowChloride, that.lowChloride) &&
                Objects.equals(dryMatter, that.dryMatter) &&
                Objects.equals(spreaderLoss, that.spreaderLoss) &&
                Objects.equals(density, that.density) &&
                Objects.equals(utilizationN, that.utilizationN) &&
                Objects.equals(utilizationNh4, that.utilizationNh4) &&
                Objects.equals(tank, that.tank) &&
                Objects.equals(electricalConductivity, that.electricalConductivity) &&
                Objects.equals(pH, that.pH) &&
                Objects.equals(solubility5C, that.solubility5C) &&
                Objects.equals(solubility20C, that.solubility20C) &&
                Objects.equals(dhCode, that.dhCode) &&
                Objects.equals(syncId, that.syncId) &&
                Objects.equals(syncSource, that.syncSource) &&
                Objects.equals(lastSync, that.lastSync) &&
//                Objects.equals(applicationTags, that.applicationTags) &&
//                Objects.equals(isAvailable, that.isAvailable) &&
//                Objects.equals(localizedName, that.localizedName) &&
//                Objects.equals(fertilizerSourceSystem, that.fertilizerSourceSystem) &&
//                Objects.equals(prodCountryIdRef, that.prodCountryIdRef) &&
//                Objects.equals(prodRegionIdRef, that.prodRegionIdRef) &&
                Objects.equals(n, that.n) &&
                Objects.equals(nUnitId, that.nUnitId) &&
                Objects.equals(p, that.p) &&
                Objects.equals(pUnitId, that.pUnitId) &&
                Objects.equals(k, that.k) &&
                Objects.equals(kUnitId, that.kUnitId) &&
                Objects.equals(mg, that.mg) &&
                Objects.equals(mgUnitId, that.mgUnitId) &&
                Objects.equals(s, that.s) &&
                Objects.equals(sUnitId, that.sUnitId) &&
                Objects.equals(ca, that.ca) &&
                Objects.equals(caUnitId, that.caUnitId) &&
                Objects.equals(b, that.b) &&
                Objects.equals(bUnitId, that.bUnitId) &&
                Objects.equals(zn, that.zn) &&
                Objects.equals(znUnitId, that.znUnitId) &&
                Objects.equals(mn, that.mn) &&
                Objects.equals(mnUnitId, that.mnUnitId) &&
                Objects.equals(cu, that.cu) &&
                Objects.equals(cuUnitId, that.cuUnitId) &&
                Objects.equals(fe, that.fe) &&
                Objects.equals(feUnitId, that.feUnitId) &&
                Objects.equals(mo, that.mo) &&
                Objects.equals(moUnitId, that.moUnitId) &&
                Objects.equals(na, that.na) &&
                Objects.equals(naUnitId, that.naUnitId) &&
                Objects.equals(se, that.se) &&
                Objects.equals(seUnitId, that.seUnitId) &&
                Objects.equals(co, that.co) &&
                Objects.equals(coUnitId, that.coUnitId) &&
                Objects.equals(no3, that.no3) &&
                Objects.equals(nh4, that.nh4) &&
                Objects.equals(urea, that.urea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nutrientUnitsContent, family, type, name, lowChloride, dryMatter, spreaderLoss, density, utilizationN, utilizationNh4, tank, electricalConductivity, pH, solubility5C, solubility20C, dhCode, syncId, syncSource, lastSync,
//                applicationTags, isAvailable, localizedName, fertilizerSourceSystem, prodCountryIdRef, prodRegionIdRef,
                n, nUnitId, p, pUnitId, k, kUnitId, mg, mgUnitId, s, sUnitId, ca, caUnitId, b, bUnitId, zn, znUnitId, mn, mnUnitId, cu, cuUnitId, fe, feUnitId, mo, moUnitId, na, naUnitId, se, seUnitId, co, coUnitId, no3, nh4, urea);
    }

    @Override
    public String toString() {
        return "Fertilizer{" +
                "nutrientUnitsContent=" + nutrientUnitsContent +
                ", family='" + family + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", lowChloride='" + lowChloride + '\'' +
                ", dryMatter='" + dryMatter + '\'' +
                ", spreaderLoss='" + spreaderLoss + '\'' +
                ", density='" + density + '\'' +
                ", utilizationN='" + utilizationN + '\'' +
                ", utilizationNh4='" + utilizationNh4 + '\'' +
                ", tank='" + tank + '\'' +
                ", electricalConductivity='" + electricalConductivity + '\'' +
                ", pH='" + pH + '\'' +
                ", solubility5C='" + solubility5C + '\'' +
                ", solubility20C='" + solubility20C + '\'' +
                ", dhCode='" + dhCode + '\'' +
                ", syncId='" + syncId + '\'' +
                ", syncSource='" + syncSource + '\'' +
                ", lastSync='" + lastSync + '\'' +
//                ", applicationTags='" + applicationTags + '\'' +
//                ", isAvailable='" + isAvailable + '\'' +
//                ", localizedName='" + localizedName + '\'' +
//                ", fertilizerSourceSystem='" + fertilizerSourceSystem + '\'' +
//                ", prodCountryIdRef='" + prodCountryIdRef + '\'' +
//                ", prodRegionIdRef='" + prodRegionIdRef + '\'' +
                ", n='" + n + '\'' +
                ", nUnitId='" + nUnitId + '\'' +
                ", p='" + p + '\'' +
                ", pUnitId='" + pUnitId + '\'' +
                ", k='" + k + '\'' +
                ", kUnitId='" + kUnitId + '\'' +
                ", mg='" + mg + '\'' +
                ", mgUnitId='" + mgUnitId + '\'' +
                ", s='" + s + '\'' +
                ", sUnitId='" + sUnitId + '\'' +
                ", ca='" + ca + '\'' +
                ", caUnitId='" + caUnitId + '\'' +
                ", b='" + b + '\'' +
                ", bUnitId='" + bUnitId + '\'' +
                ", zn='" + zn + '\'' +
                ", znUnitId='" + znUnitId + '\'' +
                ", mn='" + mn + '\'' +
                ", mnUnitId='" + mnUnitId + '\'' +
                ", cu='" + cu + '\'' +
                ", cuUnitId='" + cuUnitId + '\'' +
                ", fe='" + fe + '\'' +
                ", feUnitId='" + feUnitId + '\'' +
                ", mo='" + mo + '\'' +
                ", moUnitId='" + moUnitId + '\'' +
                ", na='" + na + '\'' +
                ", naUnitId='" + naUnitId + '\'' +
                ", se='" + se + '\'' +
                ", seUnitId='" + seUnitId + '\'' +
                ", co='" + co + '\'' +
                ", coUnitId='" + coUnitId + '\'' +
                ", no3='" + no3 + '\'' +
                ", nh4='" + nh4 + '\'' +
                ", urea='" + urea + '\'' +
                '}';
    }

    public static class Builder {
        private final Map<String, String> nutrientUnitsContent = new HashMap<>();

        private String source;
        private String className;
        private String id;
        private String family;
        private String type;
        private String name;
        private String lowChloride;
        private String dryMatter;
        private String spreaderLoss;
        private String density;
        private String utilizationN = "";
        private String utilizationNh4 = "";
        private String tank = "";
        private String electricalConductivity = "";
        private String pH = "";
        private String solubility5C = "";
        private String solubility20C = "";
        private String dhCode = "";
        private String syncId = "";
        private String syncSource = "";
        private String lastSync = "";
//        private String applicationTags = "";
//        private String isAvailable = "";
//        private String localizedName = "";
//        private String fertilizerSourceSystem = "";
//        private String prodCountryIdRef = "";
//        private String prodRegionIdRef = "";
        private String n = "";
        private String nUnitId = "";
        private String p = "";
        private String pUnitId = "";
        private String k = "";
        private String kUnitId = "";
        private String mg = "";
        private String mgUnitId = "";
        private String s = "";
        private String sUnitId = "";
        private String ca = "";
        private String caUnitId = "";
        private String b = "";
        private String bUnitId = "";
        private String zn = "";
        private String znUnitId = "";
        private String mn = "";
        private String mnUnitId = "";
        private String cu = "";
        private String cuUnitId = "";
        private String fe = "";
        private String feUnitId = "";
        private String mo = "";
        private String moUnitId = "";
        private String na = "";
        private String naUnitId = "";
        private String se = "";
        private String seUnitId = "";
        private String co = "";
        private String coUnitId = "";
        private String no3 = "";
        private String nh4 = "";
        private String urea = "";

        public Builder(String source, String className, String id, String name, String family, String type,
                       String lowChloride, String dryMatter, String spreaderLoss, String density) {
            this.source = source;
            this.className = className;
            this.id = id;
            this.name = name;
            this.family = family;
            this.type = type;
            this.lowChloride = lowChloride;
            this.dryMatter = dryMatter;
            this.spreaderLoss = spreaderLoss;
            this.density = density;
        }

        public Fertilizers build() {
            return new Fertilizers(this);
        }

        public Builder setNutrientUnitsContent(String nutrientUnitId, String val) {
            nutrientUnitsContent.put(nutrientUnitId, val);
            return this;
        }

        public Builder n(String val) {
            n = val;
            return this;
        }

        public Builder nUnitId(String val) {
            nUnitId = val;
            return this;
        }

        public Builder p(String val) {
            p = val;
            return this;
        }

        public Builder pUnitId(String val) {
            pUnitId = val;
            return this;
        }

        public Builder k(String val) {
            k = val;
            return this;
        }

        public Builder kUnitId(String val) {
            kUnitId = val;
            return this;
        }

        public Builder mg(String val) {
            mg = val;
            return this;
        }

        public Builder mgUnitId(String val) {
            mgUnitId = val;
            return this;
        }

        public Builder s(String val) {
            s = val;
            return this;
        }

        public Builder sUnitId(String val) {
            sUnitId = val;
            return this;
        }

        public Builder ca(String val) {
            ca = val;
            return this;
        }

        public Builder caUnitId(String val) {
            caUnitId = val;
            return this;
        }

        public Builder b(String val) {
            b = val;
            return this;
        }

        public Builder bUnitId(String val) {
            bUnitId = val;
            return this;
        }

        public Builder zn(String val) {
            zn = val;
            return this;
        }

        public Builder znUnitId(String val) {
            znUnitId = val;
            return this;
        }

        public Builder mn(String val) {
            mn = val;
            return this;
        }

        public Builder mnUnitId(String val) {
            mnUnitId = val;
            return this;
        }

        public Builder cu(String val) {
            cu = val;
            return this;
        }

        public Builder cuUnitId(String val) {
            cuUnitId = val;
            return this;
        }

        public Builder fe(String val) {
            fe = val;
            return this;
        }

        public Builder feUnitId(String val) {
            feUnitId = val;
            return this;
        }

        public Builder mo(String val) {
            mo = val;
            return this;
        }

        public Builder moUnitId(String val) {
            moUnitId = val;
            return this;
        }

        public Builder na(String val) {
            na = val;
            return this;
        }

        public Builder naUnitId(String val) {
            naUnitId = val;
            return this;
        }

        public Builder se(String val) {
            se = val;
            return this;
        }

        public Builder seUnitId(String val) {
            seUnitId = val;
            return this;
        }

        public Builder co(String val) {
            co = val;
            return this;
        }

        public Builder coUnitId(String val) {
            coUnitId = val;
            return this;
        }

        public Builder no3(String val) {
            no3 = val;
            return this;
        }

        public Builder nh4(String val) {
            nh4 = val;
            return this;
        }

        public Builder urea(String val) {
            urea = val;
            return this;
        }

        public Builder utilizationN(String val) {
            utilizationN = val;
            return this;
        }

        public Builder utilizationNh4(String val) {
            utilizationNh4 = val;
            return this;
        }

        public Builder tank(String val) {
            tank = val;
            return this;
        }

        public Builder electricalConductivity(String val) {
            electricalConductivity = val;
            return this;
        }

        public Builder pH(String val) {
            pH = val;
            return this;
        }

        public Builder solubility5C(String val) {
            solubility5C = val;
            return this;
        }

        public Builder solubility20C(String val) {
            solubility20C = val;
            return this;
        }

        public Builder dhCode(String val) {
            dhCode = val;
            return this;
        }

        public Builder syncId(String val) {
            syncId = val;
            return this;
        }

        public Builder syncSource(String val) {
            syncSource = val;
            return this;
        }

        public Builder lastSync(String val) {
            lastSync = val;
            return this;
        }

//        public Builder applicationTags(String val) {
//            applicationTags = val;
//            return this;
//        }
//
//        public Builder isAvailable(String val) {
//            isAvailable = val;
//            return this;
//        }
//
//        public Builder localizedName(String val) {
//            localizedName = val;
//            return this;
//        }
//
//        public Builder fertilizerSourceSystem(String val) {
//            fertilizerSourceSystem = val;
//            return this;
//        }
//
//        public Builder prodCountryIdRef(String val) {
//            prodCountryIdRef = val;
//            return this;
//        }
//
//        public Builder prodRegionIdRef(String val) {
//            prodRegionIdRef = val;
//            return this;
//        }
    }
}
