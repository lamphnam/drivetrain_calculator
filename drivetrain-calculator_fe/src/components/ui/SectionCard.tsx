import { Card, type CardProps } from './Card';

export type SectionCardProps = CardProps;

export function SectionCard(props: SectionCardProps) {
  return <Card {...props} />;
}
