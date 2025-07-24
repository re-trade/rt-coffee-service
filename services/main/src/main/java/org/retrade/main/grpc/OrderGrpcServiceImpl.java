package org.retrade.main.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.retrade.main.model.constant.OrderStatusCodes;
import org.retrade.main.repository.jpa.OrderComboRepository;
import org.retrade.proto.CountCompletedOrdersRequest;
import org.retrade.proto.CountCompletedOrdersResponse;
import org.retrade.proto.OrderServiceGrpc;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class OrderGrpcServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {
    private final OrderComboRepository orderComboRepository;

    @Override
    public void countCompletedOrders(CountCompletedOrdersRequest request, StreamObserver<CountCompletedOrdersResponse> responseObserver) {
        try {
            long count = orderComboRepository.countDistinctBySeller_IdAndOrderStatus_Code(request.getSellerId(), OrderStatusCodes.COMPLETED);
            CountCompletedOrdersResponse response = CountCompletedOrdersResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Completed orders counted successfully")
                    .setTotalOrders(count)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Completed orders counted successfully: {}", count);
        } catch (Exception e) {
            log.error("Error counting completed orders: {}", e.getMessage(), e);
            CountCompletedOrdersResponse response = CountCompletedOrdersResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to count completed orders: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
