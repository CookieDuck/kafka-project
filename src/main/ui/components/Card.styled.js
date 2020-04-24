import styled from 'styled-components';

const Wrapper = styled.div`
  layout: flex;
  flex-wrap: wrap;
  justify-content: space-evenly;
`;

const StyledCard = styled.span`
  color: ${({isRed}) => isRed ? 'red' : 'black'};
  font-size: 20px;
  display: inline-block;
  width: 72px;
`;

export {
  Wrapper,
  StyledCard,
};
