package com.gitlab.sszuev.flashcards.corlib

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
internal class CorTest {

    @Test
    fun `test single worker`() = runTest {
        val testChain = chain<TestContext> {
            worker {
                name = "test worker 1"
                description = "test worker"
                process { this.counter++ }
            }
            worker {
                name = "test worker 2"
                description = "test worker"
                test { false }
                process { this.counter++ }
            }
        }.build()

        val ctx = TestContext(counter = 0)
        testChain.exec(ctx)
        Assertions.assertEquals(1, ctx.counter)

        testChain.exec(ctx)
        Assertions.assertEquals(2, ctx.counter)
    }

    @Test
    fun `test single chain`() = runTest {
        val testChain1 = chain<TestContext> {
            chain {
                worker {
                    name = "test worker 1"
                    description = "test worker"
                    process { this.counter++ }
                }
                worker(
                    name = "test worker 2",
                    description = "test worker"
                ) {
                    this.counter++
                }
                worker(
                    name = "test worker 3",
                    description = "test worker"
                ) {
                    this.counter++
                }
            }
        }.build()

        val testChain2 = chain<TestContext> {
            chain {
                worker {
                    name = "test worker 4"
                    process { this.counter-- }
                }
                worker {
                    name = "test worker 5"
                    test {
                        this.counter < 0
                    }
                    process { this.counter++ }
                }
                worker(
                    name = "test worker 6",
                ) {
                    test {
                        this.counter > 0
                    }
                    this.counter--
                }
            }
        }.build()

        val ctx = TestContext(counter = 0)
        testChain1.exec(ctx)
        Assertions.assertEquals(3, ctx.counter)

        testChain2.exec(ctx)
        Assertions.assertEquals(1, ctx.counter)
    }

    @Test
    fun `test handle exception`() = runTest {
        val testChain1 = chain<TestContext> {
            worker {
                process { throw IllegalStateException("1") }
                onException { this.counter++ }
            }
            chain {
                name = "chain"
                worker {
                    process { throw TestException("2") }
                    onException { this.counter++ }
                }
                worker {
                    process { throw IllegalStateException("3") }
                }
            }
            worker {
                worker {
                    process { throw TestException("4") }
                    onException { this.counter++ }
                }
            }
            chain {
                worker {
                    onException { this.counter++ }
                }
            }
        }.build()

        val testChain2 = chain<TestContext> {
            worker {
                process { throw IllegalStateException("5") }
                onException { this.counter-- }
            }
            chain {
                worker {
                    process { throw TestException("6") }
                    onException { this.counter-- }
                }
                worker {
                    process { throw IllegalStateException("7") }
                    onException { this.counter-- }
                }
            }
            chain {
                worker {
                    onException { this.counter-- }
                }
            }
        }.build()

        val ctx = TestContext(counter = 0)
        assertThrows<java.lang.IllegalStateException> {
            testChain1.exec(ctx)
        }
        Assertions.assertEquals(2, ctx.counter)

        testChain2.exec(ctx)
        Assertions.assertEquals(-1, ctx.counter)
    }

    @Test
    fun `test very chained chains`() = runTest {
        val testChain = chain<TestContext> {
            chain {
                worker {
                    process { this.counter++ }
                }
                chain {
                    chain {
                        worker {
                            process { this.counter++ }
                        }
                        worker {
                            process { this.counter++ }
                        }
                    }
                    chain {
                        worker { process { this.counter *= 10 } }
                        chain {
                            worker {
                                process { this.counter++ }
                            }
                            worker {
                                process {
                                    this.counter *= 3
                                }
                                chain {
                                    worker {
                                        process {
                                            this.counter *= 2
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.build()

        val ctx = TestContext(counter = 0)
        testChain.exec(ctx)
        Assertions.assertEquals(186, ctx.counter)
    }
}

internal data class TestContext(
    var counter: Int = Int.MIN_VALUE,
)

internal class TestException(msg: String) : RuntimeException(msg)