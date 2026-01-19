package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager txManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("start");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("commit start");
        txManager.commit(status);
        log.info("commit end");
    }

    @Test
    void rollback() {
        log.info("start");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("rollback start");
        txManager.rollback(status);
        log.info("rollback end");
    }

    @Test
    void double_commit() {
        log.info("tx1 start");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("tx1 commit start");
        txManager.commit(tx1);
        log.info("tx1 commit end");

        log.info("tx2 start");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("tx2 commit start");
        txManager.commit(tx2);
        log.info("tx2 commit end");
    }

    @Test
    void commit_rollback() {
        log.info("tx1 start");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("tx1 commit start");
        txManager.commit(tx1);
        log.info("tx1 commit end");

        log.info("tx2 start");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("tx2 commit start");
        txManager.rollback(tx2);
        log.info("tx2 commit end");
    }

    @Test
    void inner_commit() {
        log.info("outer tx start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("inner start");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("inner commit start");
        txManager.commit(inner);
        log.info("inner commit end");

        log.info("outer commit start");
        txManager.commit(outer);
        log.info("outer commit end");
    }

    @Test
    void outer_rollback() {
        log.info("outer tx start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("inner start");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("inner commit start");
        txManager.commit(inner);
        log.info("inner commit end");

        log.info("outer rollabck start");
        txManager.rollback(outer);
        log.info("outer rollabck end");
    }

    @Test
    void inner_rollback() {
        log.info("outer tx start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("inner start");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("inner rollback start");
        txManager.rollback(inner);
        log.info("inner rollback end");

        log.info("outer commit start");
        Assertions.assertThatThrownBy(() -> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
        log.info("outer commit end");
    }

    @Test
    void inner_rollback_requires_new() {
        log.info("outer tx start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNewTramsaction()={}", outer.isNewTransaction());

        log.info("inner start");
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = txManager.getTransaction(definition);
        log.info("inner.isNewTramsaction()={}", inner.isNewTransaction());

        log.info("inner rollback start");
        txManager.rollback(inner);
        log.info("inner rollback end");

        log.info("outer commit start");
        txManager.commit(outer);
        log.info("outer commit end");
    }
}
