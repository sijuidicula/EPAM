package com.yara.ss.domain;

import java.util.Objects;

public class Fertilizer extends Thing {
    private String family;
    private String type;
    private String name;
    private String lowChloride;
    private String dryMatter;
    private String spreaderLoss;
    private String density;

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


    public Fertilizer(String source, String className, String id, String name, String family, String type,
                      String lowChloride, String dryMatter, String spreaderLoss, String density) {
        super(source, className, id, name);
        this.family = family;
        this.type = type;
        this.name = name;
        this.lowChloride = lowChloride;
        this.dryMatter = dryMatter;
        this.spreaderLoss = spreaderLoss;
        this.density = density;
    }

    private Fertilizer(Builder builder) {
        super(builder.source, builder.className, builder.id, builder.name);
        this.name = builder.name;
        this.family = builder.family;
        this.type = builder.type;
        this.lowChloride = builder.lowChloride;
        this.dryMatter = builder.dryMatter;
        this.spreaderLoss = builder.spreaderLoss;
        this.density = builder.density;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fertilizer that = (Fertilizer) o;
        return Objects.equals(family, that.family) &&
                Objects.equals(type, that.type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(lowChloride, that.lowChloride) &&
                Objects.equals(dryMatter, that.dryMatter) &&
                Objects.equals(spreaderLoss, that.spreaderLoss) &&
                Objects.equals(density, that.density);
    }

    @Override
    public int hashCode() {
        return Objects.hash(family, type, name, lowChloride, dryMatter, spreaderLoss, density);
    }

    @Override
    public String toString() {
        return "Fertilizer{" +
                "source='" + super.getSource() + '\'' +
                ", className='" + super.getClassName() + '\'' +
                ", uuid='" + super.getUuId() + '\'' +
                ", id='" + super.getId() + '\'' +
                ", family='" + family + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", lowChloride='" + lowChloride + '\'' +
                ", dryMatter='" + dryMatter + '\'' +
                ", spreaderLoss='" + spreaderLoss + '\'' +
                ", density='" + density + '\'' +
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

        public Fertilizer build() {
            return new Fertilizer(this);
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
            co = val;
            return this;
        }

        public Builder nh4(String val) {
            co = val;
            return this;
        }

        public Builder urea(String val) {
            co = val;
            return this;
        }
    }
}
