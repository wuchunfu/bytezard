package io.datavines.coordinator.server.dispatch.strategy;

import java.util.Collection;
import java.util.Random;

import io.datavines.coordinator.server.channel.ClientChannel;

/**
 * @author zixi0825
 */
public class RandomSelectStrategy implements IExecutorSelectStrategy<ClientChannel> {

    private final Random random = new Random();

    @Override
    public ClientChannel select(Collection<ClientChannel> list) throws IllegalArgumentException{

        if(list == null || list.size() == 0){
            throw new IllegalArgumentException("Empty list");
        }

        if(list.size() == 1){
            return (ClientChannel)list.toArray()[0];
        }

        int size = list.size();
        int randomIndex = random.nextInt(size);

        return  (ClientChannel)list.toArray()[randomIndex];
    }
}
