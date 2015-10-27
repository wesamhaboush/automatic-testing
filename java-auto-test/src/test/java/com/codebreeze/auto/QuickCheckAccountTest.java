package com.codebreeze.auto;

import com.pholser.junit.quickcheck.ForAll;
import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;
import org.junit.contrib.theories.Theories;
import org.junit.contrib.theories.Theory;
import org.junit.runner.RunWith;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class QuickCheckAccountTest {
    @Test(expected = IllegalArgumentException.class)
    public void should_not_be_able_to_create_account_with_too_short_name(){
        new Account("a", 6.0);
    }

    @Test(expected = NullPointerException.class)
    public void should_not_be_able_to_create_account_with_no_name(){
        new Account(null, 6.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_be_able_to_create_account_with_6_letters_name(){
        new Account("123456", 6.0);
    }

    @Test
    public void should_be_able_to_create_account_with_valid_name(){
        final Account account = new Account("willbe7", 6.0);
        assertEquals("willbe7", account.getUsername());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_be_able_to_create_account_with_little_money(){
        new Account("willbe7", 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_be_able_to_deposit_more_than_max(){
        new Account("happy puppy", 6.0).deposit(100001.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_be_able_to_deposit_equal_to_max(){
        new Account("happy puppy", 6.0).deposit(100000.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_be_able_to_withdraw_more_than_balance(){
        final Account account = new Account("happy puppy", 9.0);
        account.withdraw(10.0);
    }

    @Theory
    public void should_be_able_to_deposit_less_than_max(
            @ForAll @From(DepositTransactionGenerator.class) Transaction transaction) {
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
    public void should_be_able_to_withdraw_the_balance(@ForAll @From(BalanceGenerator.class) double balance) {
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
            @ForAll @From(WithdrawalTransactionGenerator.class) Transaction transaction) {
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

    public static class BalanceGenerator extends Generator<Double> {
        public BalanceGenerator() {
            super(Double.class);
        }

        @Override
        public Double generate(final SourceOfRandomness sourceOfRandomness, final GenerationStatus generationStatus) {
            return sourceOfRandomness.nextDouble(5.0, Double.MAX_VALUE);
        }
    }


}
