import java.math.BigDecimal;

/** 个人所得税计算器类
 * <br>
 * 各级税收范围和税率由税表存储。
 * 税表结构:以[该级对应应纳税所得额上界, 税率, 速算扣除数]为元素的数组
 * @author NothLeong
 * @version 1.1
 */
// TODO: 添加写入/加载配置文件逻辑，实现配置持久化
public class TaxCal {
    private BigDecimal threshold;
    private int numLevel;
    private BigDecimal[][] taxTable;

    /**
     * 构造一个未配置的TaxCal实例，需通过
     * {@link #setThreshold(BigDecimal)}、{@link #setNumLevel}、{@link #setTaxTable}完成配置才可使用。
     */
    public TaxCal() {}

    /**
     * 构造一个已配置的TaxCal实例
     * @param threshold 起征点
     * @param numLevel 税级数
     * @param taxTable 以[上界, 税率]为元素的税表数组
     */
    public TaxCal(BigDecimal threshold, int numLevel, BigDecimal[][] taxTable) {
        setThreshold(threshold);
        setNumLevel(numLevel);
        setTaxTable(taxTable);
    }

    /**
     *
     * @param threshold 个人所得税起征点
     */
    public void setThreshold(BigDecimal threshold) {
        if (threshold.compareTo(BigDecimal.ZERO)<0) {
            throw new RuntimeException("起征点不能为负数");
        }
        this.threshold = threshold;
    }

    /**
     *
     * @param numLevel 纳税分级总数
     */
    public void setNumLevel(int numLevel) {
        if (numLevel<=0) {  // 判断numLevel是否为小数由main中输入时nextInt()负责，若为小数则会自动抛出异常
            throw new RuntimeException("税率表级数必须为正整数！");
        }
        this.numLevel = numLevel;
    }

    /**
     *
     * @param taxTable 结构为以[该级应纳税所得额上界，该级税率]为元素的数组。处理时会补全速算扣除数。
     */
    public void setTaxTable(BigDecimal[][] taxTable) {
        if (taxTable.length != this.numLevel) {
            throw new RuntimeException("设置的征税梯度分级数与当前税级数不符");
        }

        this.taxTable = new BigDecimal[taxTable.length][3];

        // 当税级数为1时第一档就是最高档，应单独处理
        if (taxTable.length == 1) {
            this.taxTable[0][0] = new BigDecimal("-1");
            this.taxTable[0][1] = taxTable[0][1];
            this.taxTable[0][2] = BigDecimal.ZERO;
            return;
        }

        // 第一档速算扣除数应固定为0，同时校验第一档数据
        BigDecimal firstRate = taxTable[0][1];
        BigDecimal firstUpper = taxTable[0][0];
        if (firstUpper.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("第1档上界必须大于0");
        }
        if (firstRate.compareTo(BigDecimal.ZERO) <= 0 || firstRate.compareTo(BigDecimal.ONE) > 0) {
            throw new RuntimeException("第1档税率必须在(0, 1]之间");
        }
        this.taxTable[0][0] = firstUpper;
        this.taxTable[0][1] = firstRate;
        this.taxTable[0][2] = BigDecimal.ZERO;

        BigDecimal prevDeduction = BigDecimal.ZERO;
        BigDecimal prevRate = taxTable[0][1];
        BigDecimal prevUpper = taxTable[0][0];
        int last = taxTable.length - 1;

        // 计算速算扣除数,同时校验每档数据。需要注意的是，税表需要对最高档单独处理
        for (int i = 1; i < last; i++) {
            BigDecimal upper = taxTable[i][0];
            BigDecimal rate  = taxTable[i][1];

            if (upper.compareTo(prevUpper) <= 0) {
                throw new RuntimeException("第" + (i + 1) + "档上界必须大于下界");
            }
            if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
                throw new RuntimeException("第" + (i + 1) + "档税率必须在(0, 1]之间");
            }

            BigDecimal deduction = prevDeduction.add(prevUpper.multiply(rate.subtract(prevRate)));

            this.taxTable[i][0] = upper;
            this.taxTable[i][1] = rate;
            this.taxTable[i][2] = deduction;

            prevDeduction = deduction;
            prevRate = rate;
            prevUpper = upper;
        }

        BigDecimal lastRate = taxTable[last][1];
        if (lastRate.compareTo(BigDecimal.ZERO) <= 0 || lastRate.compareTo(BigDecimal.ONE) > 0) {
            throw new RuntimeException("第" + (last + 1) + "档税率必须在(0, 1]之间");
        }
        BigDecimal lastDeduction = prevDeduction.add(prevUpper.multiply(lastRate.subtract(prevRate)));
        this.taxTable[last][0] = new BigDecimal("-1");
        this.taxTable[last][1] = lastRate;
        this.taxTable[last][2] = lastDeduction;
    }

    /**
     *
     * @return 当前计算器设置的起征点
     */
    public BigDecimal getThreshold() {
        return threshold;
    }

    /**
     *
     * @return 当前计算器设置的税级数
     */
    public int getNumLevel() {
        return numLevel;
    }

    /**
     *
     * @return 当前计算器设置的税表的深拷贝
     */
    public BigDecimal[][] getTaxTable() {
        BigDecimal[][] copy = new BigDecimal[taxTable.length][];
        for (int i = 0; i < taxTable.length; i++) {
            copy[i] = taxTable[i].clone();
        }
        return copy;
    }

    /**
     * @return 当前计算器是否已完成配置
     */
    public boolean isConfigured() {
        return numLevel != 0 && threshold != null && taxTable != null;
    }

    /**
     *
     * @param salary 原始薪水
     * @return 应纳个人所得税
     */
    public BigDecimal calTax(BigDecimal salary) {
        if (salary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("薪资必须大于0");
        }
        BigDecimal payablePart = salary.subtract(threshold);

        if (payablePart.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        for (BigDecimal[] bracket : taxTable) {
            if (bracket[0].compareTo(BigDecimal.ZERO) < 0
                    || payablePart.compareTo(bracket[0]) <= 0) {
                return payablePart.multiply(bracket[1]).subtract(bracket[2]);
            }
        }
        throw new RuntimeException("未找到匹配的税档，请检查税表配置");
    }
}
