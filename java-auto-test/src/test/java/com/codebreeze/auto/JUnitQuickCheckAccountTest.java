package com.codebreeze.auto;

import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Rule;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

/*
 * https://github.com/pholser/junit-quickcheck
 * https://bitbucket.org/blob79/quickcheck
 */
@RunWith(Theories.class)
public class JUnitQuickCheckAccountTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Theory
    public void should_not_be_able_to_create_account_with_6_or_fewer_letters_name(
            @ForAll
            @From(InvalidUsernameGenerator.class)
            String username) {
        //given
        assumeThat(StringUtils.length(username), lessThanOrEqualTo(6));

        //when
        exception.expect(username == null ? NullPointerException.class : IllegalArgumentException.class);
        new Account(username, 50.0);

        //then
        fail("should have never been here");
    }

    @Theory
    public void should_be_able_to_create_account_with_valid_name(
            @ForAll @From(UsernameGenerator.class)
            String username) {
        //given
        assumeThat(username.length(), greaterThan(6));

        //when
        final Account account = new Account(username, 50.0);


        //then
        assertEquals(username, account.getUsername());
    }

    @Theory
    public void should_not_be_able_to_create_account_with_little_money(
            @ForAll
            @From(InvalidBalanceGenerator.class)
            double initialBalance) {
        //given
        assumeThat(initialBalance, greaterThanOrEqualTo(0.0));
        assumeThat(initialBalance, lessThanOrEqualTo(5.0));

        //when
        exception.expect(IllegalArgumentException.class);
        new Account(randomAlphanumeric(7), initialBalance);

        //then
        fail("should have never been here");
    }

    @Theory
    public void should_not_be_able_to_deposit_more_than_or_equal_to_max(
            @ForAll
            @From(InvalidDepositTransactionGenerator.class)
            Transaction transaction) {
        //given
        assumeThat(transaction.balance, greaterThanOrEqualTo(0.0));
        assumeThat(transaction.amount, allOf(greaterThan(0.0), greaterThan(transaction.balance)));

        //when
        final Account account = new Account(randomAlphanumeric(7), transaction.balance);
        exception.expect(IllegalArgumentException.class);
        account.deposit(transaction.amount);

        //then
        fail("should have never been here");
    }

    @Theory
    public void should_not_be_able_to_withdraw_more_than_balance(
            @ForAll
            @From(InvalidWithdrawalTransactionGenerator.class)
            Transaction transaction) {
        //given
        assumeThat(transaction.balance, greaterThanOrEqualTo(0.0));
        assumeThat(transaction.amount, allOf(greaterThan(0.0), greaterThan(transaction.balance)));

        //when
        final Account account = new Account(randomAlphanumeric(7), transaction.balance);
        exception.expect(IllegalArgumentException.class);
        account.withdraw(transaction.amount);

        //then
        fail("should have never been here");
    }

    @Theory
    public void should_be_able_to_deposit_less_than_max(
            @ForAll
            @From(DepositTransactionGenerator.class)
            Transaction transaction) {
        //given
        assumeThat(transaction.balance, greaterThanOrEqualTo(0.0));
        assumeThat(transaction.amount, allOf(greaterThan(0.0), lessThanOrEqualTo(transaction.balance)));

        //when
        final Account account = new Account(randomAlphanumeric(7), transaction.balance);
        account.deposit(transaction.amount);


        //then
        assertEquals(transaction.balance + transaction.amount, account.getBalance(), 0.01);
    }

    @Theory
    public void should_be_able_to_withdraw_the_balance(
            @ForAll
            @From(BalanceGenerator.class)
            double balance) {
        //given
        assumeThat(balance, greaterThanOrEqualTo(0.0));

        //when
        final Account account = new Account(randomAlphanumeric(7), balance);
        account.withdraw(balance);


        //then
        assertEquals(0.0, account.getBalance(), 0.01);
    }

    @Theory
    public void should_be_able_to_withdraw_less_than_balance(
            @ForAll @From(WithdrawalTransactionGenerator.class)
            Transaction transaction) {
        //given
        assumeThat(transaction.balance, greaterThanOrEqualTo(0.0));
        assumeThat(transaction.amount, allOf(greaterThan(0.0), lessThanOrEqualTo(transaction.balance)));

        //when
        final Account account = new Account(randomAlphanumeric(7), transaction.balance);
        account.withdraw(transaction.amount);


        //then
        assertEquals(transaction.balance - transaction.amount, account.getBalance(), 0.01);
    }

    //utils

    public static class Transaction {
        enum Type { WITHDRAWAL, DEPOSIT}
        private final double balance;
        private final double amount;
        private final Type type;

        private Transaction(final double balance, final double amount, final Type type) {
            this.balance = balance;
            this.amount = amount;
            this.type = type;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("balance", balance)
                    .append("amount", amount)
                    .append("type", type)
                    .toString();
        }
    }

    public static class WithdrawalTransactionGenerator extends Generator<Transaction> {
        public WithdrawalTransactionGenerator() {
            super(Transaction.class);
        }

        @Override
        public Transaction generate(final SourceOfRandomness sourceOfRandomness, final GenerationStatus generationStatus) {
            final double withdrawalAmount = sourceOfRandomness.nextDouble(1, Double.MAX_VALUE - 1000);
            final double  initialBalance = sourceOfRandomness.nextDouble(withdrawalAmount, Double.MAX_VALUE);
            return new Transaction(initialBalance, withdrawalAmount, Transaction.Type.WITHDRAWAL);
        }
    }

    public static class InvalidWithdrawalTransactionGenerator extends Generator<Transaction> {
        public InvalidWithdrawalTransactionGenerator() {
            super(Transaction.class);
        }

        @Override
        public Transaction generate(final SourceOfRandomness sourceOfRandomness, final GenerationStatus generationStatus) {
            final double initialBalance = sourceOfRandomness.nextDouble(1, Double.MAX_VALUE - 1000);
            final double  withdrawalAmount = sourceOfRandomness.nextDouble(initialBalance + 0.01, Double.MAX_VALUE);
            return new Transaction(initialBalance, withdrawalAmount, Transaction.Type.WITHDRAWAL);
        }
    }

    public static class DepositTransactionGenerator extends Generator<Transaction> {
        public DepositTransactionGenerator() {
            super(Transaction.class);
        }

        @Override
        public Transaction generate(final SourceOfRandomness sourceOfRandomness, final GenerationStatus generationStatus) {
            final double balance = sourceOfRandomness.nextDouble(0.0, Double.MAX_VALUE - 100000);
            final double  depositAmount = sourceOfRandomness.nextDouble(0, 100000);
            return new Transaction(balance, depositAmount, Transaction.Type.DEPOSIT);
        }
    }

    public static class InvalidDepositTransactionGenerator extends Generator<Transaction> {
        public InvalidDepositTransactionGenerator() {
            super(Transaction.class);
        }

        @Override
        public Transaction generate(final SourceOfRandomness sourceOfRandomness, final GenerationStatus generationStatus) {
            final double balance = sourceOfRandomness.nextDouble(0.0, Double.MAX_VALUE);
            final double  depositAmount = sourceOfRandomness.nextDouble(100000, Double.MAX_VALUE);
            return new Transaction(balance, depositAmount, Transaction.Type.DEPOSIT);
        }
    }

    public static class BalanceGenerator extends Generator<Double> {
        public BalanceGenerator() {
            super(Double.class);
        }

        @Override
        public Double generate(final SourceOfRandomness sourceOfRandomness, final GenerationStatus generationStatus) {
            return sourceOfRandomness.nextDouble(5.0, Double.MAX_VALUE);
        }
    }

    public static class InvalidBalanceGenerator extends Generator<Double> {
        public InvalidBalanceGenerator() {
            super(Double.class);
        }

        @Override
        public Double generate(final SourceOfRandomness sourceOfRandomness, final GenerationStatus generationStatus) {
            return sourceOfRandomness.nextDouble(0.0, 5.0);
        }
    }

    public static class UsernameGenerator extends Generator<String> {
        public UsernameGenerator() {
            super(String.class);
        }

        @Override
        public String generate(final SourceOfRandomness sourceOfRandomness, final GenerationStatus generationStatus) {
            return randomAlphanumeric(sourceOfRandomness.nextInt(7, 200));
        }
    }

    public static class InvalidUsernameGenerator extends Generator<String> {
        private boolean haveReturnedNull = false;
        private boolean haveReturnedEmpty = false;

        public InvalidUsernameGenerator() {
            super(String.class);
        }

        @Override
        public String generate(final SourceOfRandomness sourceOfRandomness, final GenerationStatus generationStatus) {
            if(!haveReturnedNull){
                haveReturnedNull = true;
                return null;
            }
            if(!haveReturnedEmpty){
                haveReturnedEmpty = true;
                return "";
            }
            return randomAlphanumeric(sourceOfRandomness.nextInt(0, 7));
        }
    }
}
