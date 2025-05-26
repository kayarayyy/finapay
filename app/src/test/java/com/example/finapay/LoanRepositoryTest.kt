package com.example.finapay.data.repositories

import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.sources.remote.LoanService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import retrofit2.Call

@OptIn(ExperimentalCoroutinesApi::class)
class LoanRepositoryTest {

    private lateinit var loanService: LoanService
    private lateinit var loanRepository: LoanRepository

    @Before
    fun setUp() {
        loanService = mockk()
        loanRepository = LoanRepository(loanService)
    }

    @Test
    fun `getAllLoanRequestByEmail returns expected data`() = runTest {
        val loan = LoanModel(
            id = "123",
            amount = "1000000",
            instalment = "100000",
            tenor = "10",
            title = "Personal Loan",
            backOfficeDisbursedAt = "2025-05-01",
            status = "APPROVED",
            isApproved = true
        )
        val expectedResponse = ApiResponse(
            status = "success",
            message = "Data fetched successfully",
            data = listOf(loan),
            status_code = 200
        )

        coEvery { loanService.getAllLoanRequestByEmail() } returns expectedResponse

        val result = loanRepository.getAllLoanRequestByEmail()

        assertEquals(expectedResponse, result)
        coVerify { loanService.getAllLoanRequestByEmail() }
    }

    @Test
    fun `getAllLoanRequestByEmailAndStatus returns filtered data`() = runTest {
        val status = "PENDING"
        val loan = LoanModel(
            id = "456",
            amount = "500000",
            instalment = "50000",
            tenor = "10",
            title = "Car Loan",
            backOfficeDisbursedAt = "2025-04-20",
            status = status,
            isApproved = false
        )
        val expectedResponse = ApiResponse(
            status = "success",
            message = "Data fetched successfully",
            data = listOf(loan),
            status_code = 200
        )

        coEvery { loanService.getAllLoanRequestByEmail(status) } returns expectedResponse

        val result = loanRepository.getAllLoanRequestByEmailAndStatus(status)

        assertEquals(expectedResponse, result)
        coVerify { loanService.getAllLoanRequestByEmail(status) }
    }

    @Test
    fun `postLoanRequest returns correct call`() {
        val mockCall = mockk<Call<ApiResponse<LoanModel>>>()
        val requestBody = mockk<RequestBody>()

        every {
            loanService.postLoanRequest(
                any(), any(), any(), any(), any()
            )
        } returns mockCall

        val result = loanRepository.postLoanRequest(
            requestBody, requestBody, requestBody, requestBody, requestBody
        )

        assertEquals(mockCall, result)
        verify {
            loanService.postLoanRequest(
                requestBody, requestBody, requestBody, requestBody, requestBody
            )
        }
    }
}
