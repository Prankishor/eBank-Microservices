package com.kishore.accounts.service.impl;

import com.kishore.accounts.dto.AccountsDto;
import com.kishore.accounts.dto.CardsDto;
import com.kishore.accounts.dto.CustomerDetailsDto;
import com.kishore.accounts.dto.LoansDto;
import com.kishore.accounts.entity.Accounts;
import com.kishore.accounts.entity.Customer;
import com.kishore.accounts.exception.ResourceNotFoundException;
import com.kishore.accounts.mapper.AccountsMapper;
import com.kishore.accounts.mapper.CustomerMapper;
import com.kishore.accounts.repository.AccountsRepository;
import com.kishore.accounts.repository.CustomerRepository;
import com.kishore.accounts.service.ICustomersService;
import com.kishore.accounts.service.client.CardsFeignClient;
import com.kishore.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                () -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString())
        );

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(mobileNumber);

        if(null != loansDtoResponseEntity) {
            customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());
        }

        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(mobileNumber);
        if(null != cardsDtoResponseEntity) {
            customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());
        }

        return customerDetailsDto;
    }
}
