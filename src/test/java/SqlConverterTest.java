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
                FROM            dbo.clx_transactions_micamp LEFT OUTER JOIN
                                             dbo.vw_clx_summary_by_day_rapiddeposit ON dbo.clx_transactions_micamp.[Funded Date] = dbo.vw_clx_summary_by_day_rapiddeposit.Day AND
                                             dbo.clx_transactions_micamp.[Site ID] = dbo.vw_clx_summary_by_day_rapiddeposit.[Site ID] LEFT OUTER JOIN
                                             dbo.vw_clx_discount_frequency ON dbo.clx_transactions_micamp.[Site ID] = dbo.vw_clx_discount_frequency.MID
                GROUP BY dbo.clx_transactions_micamp.[Site ID], dbo.clx_transactions_micamp.[Site Name], eomonth(dbo.clx_transactions_micamp.[Funded Date], 0), dbo.vw_clx_summary_by_day_rapiddeposit.Amount
        """;
        String mysql = """
            SELECT clx_statement_periods.statement_period_transactions AS statement_period,
                    clx_transactions_micamp.site_id,
                    clx_transactions_micamp.site_name,
                    clx_transactions_micamp.network AS product
                    FROM            clx_transactions_micamp LEFT OUTER JOIN
                                                 vw_clx_summary_by_day_rapiddeposit ON clx_transactions_micamp.funded_date = vw_clx_summary_by_day_rapiddeposit.day AND
                                                 clx_transactions_micamp.site_id = vw_clx_summary_by_day_rapiddeposit.site_id LEFT OUTER JOIN
                                                 vw_clx_discount_frequency ON clx_transactions_micamp.site_id = vw_clx_discount_frequency.mid
                    GROUP BY clx_transactions_micamp.site_id, clx_transactions_micamp.site_name, LAST_DAY(DATE_ADD(clx_transactions_micamp.funded_date, INTERVAL 0 MONTH)), vw_clx_summary_by_day_rapiddeposit.amount""";
        String mysqlQuery = SqlConverter.convertMssqlToMysql(mssqlQuery);
        System.out.println(mysqlQuery);
        assert mysqlQuery.equals(mysql);
    }
}
