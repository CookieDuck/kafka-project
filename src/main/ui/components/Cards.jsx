import React from 'react';
import Card from './Card';
import { Wrapper} from './Card.styled';

const Cards = ({cards}) => {
  return(
    <Wrapper>
      {cards.map(card =>
        <Card
          suit={card.suit}
          value={card.value}
          key={`${card.suit}-${card.value}`}
        />)}
    </Wrapper>
  );
};

export default Cards;
