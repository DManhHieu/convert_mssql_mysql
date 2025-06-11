import com.dhieu.converter.converter.TsToJavaConverter;
import org.junit.Test;

public class TsToJavaConverterTest {
    @Test
    public void testConvertSql() {
        String input = """
                export interface IFiservChargebacksData {
                    amount: number;
                    cardNumber: string;
                    description: string;
                    referenceNumber: string;
                    date: string;
                    isDisputed: boolean;
                    values: number[];
                }
        """;
        String expected = """
            import java.math.BigDecimal;
            import java.util.List;

            public class FiservChargebacksDataDTO {
                private BigDecimal amount;
                private String cardNumber;
                private String description;
                private String referenceNumber;
                private String date;
                private Boolean isDisputed;
                private List<BigDecimal> values;
            }""";
        String output = TsToJavaConverter.convert(input);
        System.out.println(output);
        System.out.println(expected);
        assert output.equals(expected);
    }
}
