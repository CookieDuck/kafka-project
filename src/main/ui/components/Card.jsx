import React from 'react';
import Emoji from 'a11y-react-emoji';
import { Wrapper, StyledCard } from './Card.styled';

const getSuitEmoji = suit => {
  switch (suit) {
    case 'c':
    case 'club':
    case 'clubs':
      return <Emoji label='clubs' symbol="♣️" />;
    case 'h':
    case 'heart':
    case 'hearts':
      return <Emoji label='hearts' symbol="♥️" />;
    case 's':
    case 'spade':
    case 'spades':
      return <Emoji label='spades' symbol="♠️️" />;
    case 'd':
    case 'diamond':
    case 'diamonds':
      return <Emoji label='diamonds' symbol="♦️️" />;
    default:
      return <div>{`Don't know about ${suit}`}</div>;
  }
};

const getRedness = suit => {
  switch (suit) {
    case 'd':
    case 'diamond':
    case 'diamonds':
    case 'h':
    case 'heart':
    case 'hearts':
      return true;
    default:
      return false;
  }
};

const Card = ({suit, value}) => {
  return (
    <StyledCard
      isRed={getRedness(suit)}
    >
      {`${value} of `}
      {getSuitEmoji(suit)}
    </StyledCard>
  );
};

export default Card;
