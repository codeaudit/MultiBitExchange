package org.multibit.exchange.infrastructure.service;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.repository.AggregateNotFoundException;
import org.multibit.exchange.domain.command.CreateExchangeCommand;
import org.multibit.exchange.domain.command.CurrencyPairDescriptor;
import org.multibit.exchange.domain.command.ExchangeCommand;
import org.multibit.exchange.domain.command.ExchangeId;
import org.multibit.exchange.domain.command.OrderDescriptor;
import org.multibit.exchange.domain.command.OrderId;
import org.multibit.exchange.domain.command.PlaceOrderCommand;
import org.multibit.exchange.domain.command.RegisterTickerCommand;
import org.multibit.exchange.service.ExchangeService;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * <p>Service to provide the following to the application:</p>
 * <ul>
 * <li>Concrete implementation of {@link org.multibit.exchange.service.ExchangeService} based on the Axon Framework</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class AxonEventBasedExchangeService implements ExchangeService {

  private final CommandGateway commandGateway;

  private static final long TIMEOUT = 1;

  @Inject
  public AxonEventBasedExchangeService(CommandGateway commandGateway) {

    this.commandGateway = commandGateway;
  }

  @Override
  public void initializeExchange(ExchangeId identifier) {
    CreateExchangeCommand command = new CreateExchangeCommand(identifier);
    safeSendAndWait(command);
  }

  @Override
  public void registerTicker(ExchangeId exchangeId, CurrencyPairDescriptor currencyPair) {
    RegisterTickerCommand command = new RegisterTickerCommand(exchangeId, currencyPair);
    safeSendAndWait(command);
  }

  @Override
  public void placeOrder(ExchangeId exchangeId, OrderId orderId, OrderDescriptor order) {
    PlaceOrderCommand command = new PlaceOrderCommand(exchangeId, order);
    safeSendAndWait(command);
  }

  private void safeSendAndWait(ExchangeCommand command) {
    try {
      commandGateway.sendAndWait(command, TIMEOUT, TimeUnit.SECONDS);
    } catch (AggregateNotFoundException e) {
      throw new NoSuchExchangeException(command.getExchangeId(), e);
    }
  }

  @Override
  public String toString() {
    return "AxonEventBasedExchangeService{" +
        "commandGateway=" + commandGateway +
        '}';
  }
}
