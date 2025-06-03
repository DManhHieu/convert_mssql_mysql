import com.dhieu.mssqltomysql.SqlConverter;
import org.junit.Test;

public class SqlConverterTest {
    @Test
    public void testConvertSql() {
        String mssqlQuery = """
                SELECT        TOP (100) PERCENT dbo.clx_statement_periods.[Statement Period - Transactions] AS [Statement Period],
                dbo.clx_transactions_micamp.[Site ID],
                dbo.clx_transactions_micamp.[Site Name],
                dbo.clx_transactions_micamp.Network AS Product
        """;
        String mysql = """
        SELECT clx_statement_periods.statement_period_transactions AS statement_period,
                clx_transactions_micamp.site_id,
                clx_transactions_micamp.site_name,
                clx_transactions_micamp.network AS product""";
        String mysqlQuery = SqlConverter.convertMssqlToMysql(mssqlQuery);
        System.out.println(mysqlQuery);
        assert mysqlQuery.equals(mysql);
    }
}
