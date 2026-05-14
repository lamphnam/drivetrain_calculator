import { StyleSheet } from 'react-native';

import { Card } from '@/components/ui/Card';
import { KeyValueList } from '@/components/ui/KeyValueList';
import { Text } from '@/components/ui/Text';
import { UI_TEXT } from '@/constants/uiText';
import { appTheme } from '@/theme';

type ShaftDetailCardProps = {
  title: string;
  power: string;
  rpm: string;
  torque: string;
  summary?: string;
};

export function ShaftDetailCard({
  title,
  power,
  rpm,
  torque,
  summary,
}: ShaftDetailCardProps) {
  return (
    <Card tone="muted" style={styles.card}>
      <Text variant="bodyStrong">{title}</Text>
      <KeyValueList
        items={[
          { label: UI_TEXT.results.power, value: power },
          { label: UI_TEXT.results.speed, value: rpm },
          { label: UI_TEXT.results.torque, value: torque },
        ]}
      />
      {summary ? (
        <Text variant="caption" tone="secondary">
          {summary}
        </Text>
      ) : null}
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: appTheme.spacing.sm,
  },
});
