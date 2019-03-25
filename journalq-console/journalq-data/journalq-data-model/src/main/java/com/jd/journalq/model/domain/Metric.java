package com.jd.journalq.model.domain;

/**
 * Metric
 * Created by chenyanying3 on 19-2-25.
 */
public class Metric extends BaseModel implements Identifier, Cloneable {

    /**
     * metric code, abbr.
     */
    private String code;
    /**
     * metric aliasCode, inner used, unique
     */
    private String aliasCode;
    /**
     * metric name
     */
    private String name;
    /**
     * metric type, atomic or aggregator
     */
    private Integer type;
    /**
     * only for aggregator metric, which describe metric's origin metric code
     */
    private String source;
    /**
     * describe metric aggregate method or others
     */
    private String description;
    /**
     * metric provider
     */
    private String provider;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getAliasCode() {
        return aliasCode;
    }

    public void setAliasCode(String aliasCode) {
        this.aliasCode = aliasCode;
    }

    @EnumType(MetricType.class)
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public enum MetricType implements EnumItem{
        OTHERS(0, "others"),
        ATOMIC(1, "atomic"),
        AGGREGATOR(2, "aggregator");

        private int value;
        private String description;

        MetricType(int value, String description) {
            this.value = value;
            this.description = description;
        }

        public int value() {
            return this.value;
        }

        public String description() {
            return this.description;
        }

        public static MetricType resolve(int value) {
            for (MetricType type : MetricType.values()) {
                if (type.value() == value) {
                    return type;
                }
            }
            return OTHERS;
        }

        public static MetricType resolve(String descOrName) {
            for (MetricType type : MetricType.values()) {
                if (type.description().equals(descOrName) || type.name().equals(descOrName)) {
                    return type;
                }
            }
            return OTHERS;
        }
    }

//    public enum ChartType {
//        OTHERS(0, "others"),
//        PRODUCER_DETAIL(1, "pd"),
//        CONSUMER_DETAIL(2, "cd"),
//        PRODUCER_TOTAL(3, "pt"),
//        CONSUMER_TOTAL(4, "ct"),
//        HOST(5, "host"),
//        BROKER(6, "broker");
//
//        private int value;
//        private String chart;
//
//        ChartType(int value, String chart) {
//            this.value = value;
//            this.chart = chart;
//        }
//
//        public int value() {
//            return this.value;
//        }
//
//        public int valueOf() {
//            return this.value;
//        }
//
//        public String chart() {
//            return this.chart;
//        }
//
//        public static ChartType resolve(int value) {
//            for (ChartType type : ChartType.values()) {
//                if (type.value() == value) {
//                    return type;
//                }
//            }
//            return OTHERS;
//        }
//
//        public static ChartType resolve(String chartOrName) {
//            for (ChartType type : ChartType.values()) {
//                if (type.chart().equals(chartOrName) || type.name().equals(chartOrName)) {
//                    return type;
//                }
//            }
//            return OTHERS;
//        }
//    }

}
