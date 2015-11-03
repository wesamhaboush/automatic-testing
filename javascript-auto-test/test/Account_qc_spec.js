require('expectations');
var JSC = require('../jscheck');

describe('Account Specs:', function() {

    var Account = require('../src/Account');
    var report = function(result) {
//        console.log(result);
    };
    var verifyResults = function(done) {
        return function(result) {
            console.log(JSON.stringify(result));
            expect(result.pass).toBeGreaterThan(0);
            expect(result.pass).toEqual(result.total);
            expect(result.ok).toBe(true);
            done();
        };
    };

    it('Account username cannot be too short, i.e. less than 7 characters', function(done) {
        var newAccount = function(username) {
            try {
                new Account({
                    username: username,
                    initialBalance: 6.0
                });
                console.log("should not be here, user name with value has failed:", username);
                return false;
            } catch (e) {
                return true;
            }
        };
        JSC.on_report(report)
            .on_result(verifyResults(done))
            .test(
                "Account username cannot be too short, i.e. less than 7 characters",
                function(verdict, username) {
                    return verdict(newAccount(username));
                }, [
                    JSC.one_of([
                        JSC.one_of([undefined, null, '']),
                        JSC.string(1, JSC.character('a', 'z')),
                        JSC.string(2, JSC.character('a', 'z')),
                        JSC.string(3, JSC.character('a', 'z')),
                        JSC.string(4, JSC.character('a', 'z')),
                        JSC.string(5, JSC.character('a', 'z')),
                        JSC.string(6, JSC.character('a', 'z')),
                    ])
                ],
                function(a) {
                    if (!a) {
                        return a;
                    } else {
                        return a.toString().length.toString();
                    }
                }
            );
    });

    it('should be able to create account with valid name', function(done) {
        var newAccount = function(username) {
            try {
                new Account({
                    username: username,
                    initialBalance: 6.0
                });
                return true;
            } catch (e) {
                console.log(e);
                console.log("couldn't create account with username: ", username);
                return false;
            }
        };
        JSC.on_report(report)
            .on_result(verifyResults(done))
            .test(
                "should be able to create account with valid names (greater than 6 letters)",
                function(verdict, username) {
                    return verdict(newAccount(username));
                }, [JSC.string(JSC.integer(7, 10), JSC.character('a', 'z'))],
                function(a) {
                    if (!a) {
                        return a;
                    } else {
                        return a.toString().length.toString();
                    }
                }
            );
    });

    it('should not be able to create account with little money, i.e < 6$', function(done) {
        var newAccount = function(initialBalance) {
            try {
                new Account({
                    username: '1234567',
                    initialBalance: initialBalance
                });
                console.log("should not have been able to create account with amount: ", initialBalance);
                return false;
            } catch (e) {
                return true;
            }
        };
        JSC.on_report(report)
            .on_result(verifyResults(done))
            .test(
                "should not be able to create account with little money, i.e < 6$",
                function(verdict, initialBalance) {
                    return verdict(newAccount(initialBalance));
                }, [
                    JSC.number(5.0)
                ],
                function(a) {
                    return Math.floor(a).toString();
                }
            );
    });

    it('should not be able to deposit into an account more than or equal to max, i.e. 100 000$', function(done) {
        var deposit = function(amount) {
            var newAccount = new Account({
                username: '1234567',
                initialBalance: 6.0
            });
            try {
                newAccount.deposit(amount);
                console.log("should not have been able to deposit more than the max, i.e.: ", amount);
                return false;
            } catch (e) {
                return true;
            }
        };
        JSC.on_report(report)
            .on_result(verifyResults(done))
            .test(
                "should not be able to deposit into an account more than or equal to max, i.e. 100 000$",
                function(verdict, amount) {
                    return verdict(deposit(amount));
                }, [
                    JSC.number(100000, 1000000)
                ],
                function(a) {
                    return Math.floor(a).toString();
                }
            );
    });

    it('should be able to deposit into an account an amount less than max', function(done) {
        var deposit = function(amount) {
            var newAccount = new Account({
                username: '1234567',
                initialBalance: 6.0
            });
            try {
                newAccount.deposit(amount);
                return true;
            } catch (e) {
                console.log(e);
                console.log("should have been able to deposit less than the max, i.e.: ", amount);
                return false;
            }
        };
        JSC.on_report(report)
            .on_result(verifyResults(done))
            .test(
                "should be able to deposit into an account an amount less than max",
                function(verdict, amount) {
                    return verdict(deposit(amount));
                }, [
                    JSC.number(99999.99)
                ],
                function(a) {
                    return Math.floor(a).toString();
                }
            );
    });

    it('should not be able to withdraw from an account more than balance', function(done) {
        var initialBalance = JSC.number(5.1, 100000)();
        var withdraw = function(amount) {
            var newAccount = new Account({
                username: '1234567',
                initialBalance: initialBalance
            });
            try {
                newAccount.withdraw(amount);
                console.log("should not have been able to withdraw more than the balance, i.e.: ", amount);
                return false;
            } catch (e) {
                return true;
            }
        };
        JSC.on_report(report)
            .on_result(verifyResults(done))
            .test(
                "should not be able to withdraw from an account more than balance",
                function(verdict, amount) {
                    return verdict(withdraw(amount));
                }, [
                    JSC.number(initialBalance + 0.1, 2 * initialBalance) //arbitrarily any number above balance and up to double the balance
                ],
                function(a) {
                    return Math.floor(a).toString();
                }
            );
    });


    it('should be able to withdraw from an account all the balance or less', function(done) {
        var initialBalance = JSC.number(5.1, 1000000)();
        var withdraw = function(amount) {
            var newAccount = new Account({
                username: '1234567',
                initialBalance: initialBalance
            });
            try {
                newAccount.withdraw(amount);
                return true;
            } catch (e) {
                console.log(e);
                console.log("should have beeen able to withdraw from an account all the balance or less, but could not for ", amount);
                return false;
            }
        };
        JSC.on_report(report)
            .on_result(verifyResults(done))
            .test(
                "should be able to withdraw from an account all the balance or less",
                function(verdict, amount) {
                    return verdict(withdraw(amount));
                }, [
                    JSC.number(initialBalance)
                ],
                function(a) {
                    return Math.floor(a).toString();
                }
            );
    });
});
